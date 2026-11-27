package com.bigcash.app.data

import com.bigcash.app.data.model.AppSettings
import com.bigcash.app.di.IoDispatcher
import com.bigcash.core.database.RawNotificationDao
import com.bigcash.core.database.RawNotificationEntity
import com.bigcash.core.database.TransactionDao
import com.bigcash.core.model.ParseOutcome
import com.bigcash.core.model.ParseStatus
import com.bigcash.core.model.RawNotification
import com.bigcash.core.model.Transaction
import com.bigcash.core.parsing.ParsingEngine
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** What happened with a notification that reached the listener. */
sealed interface IngestResult {
    data class NotMonitored(val packageName: String) : IngestResult
    data class Duplicate(val raw: RawNotification) : IngestResult
    data class Recorded(val raw: RawNotification, val transaction: Transaction) : IngestResult
    data class Partial(val raw: RawNotification, val outcome: ParseOutcome) : IngestResult
    data class Failed(val raw: RawNotification, val outcome: ParseOutcome) : IngestResult
}

/** Result of replaying the whole history through the current rules. */
data class ReprocessReport(
    val processed: Int,
    val succeeded: Int,
    val partial: Int,
    val failed: Int,
)

/**
 * Ingests notifications: raw capture, deduplication, parsing and transaction creation.
 * Raw rows are always stored, even when nothing can be parsed.
 */
@Singleton
class NotificationIngestRepository @Inject constructor(
    private val rawNotificationDao: RawNotificationDao,
    private val transactionDao: TransactionDao,
    private val ruleRepository: RuleRepository,
    private val packageRepository: PackageRepository,
    private val settingsRepository: SettingsRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {

    suspend fun ingest(
        packageName: String,
        title: String?,
        text: String?,
        bigText: String?,
        postedAt: Long,
    ): IngestResult = withContext(dispatcher) {
        val monitored = packageRepository.enabledPackage(packageName)
            ?: return@withContext IngestResult.NotMonitored(packageName)

        val settings = settingsRepository.current()
        val hash = hashOf(packageName, title, text, bigText)
        val window = settings.parsing.dedupeWindowMillis
        rawNotificationDao.findRecentByHash(hash, postedAt - window)?.let { existing ->
            return@withContext IngestResult.Duplicate(existing.toDomain())
        }

        val rawId = rawNotificationDao.insert(
            RawNotificationEntity(
                packageName = packageName,
                bankKey = monitored.bankKey,
                title = title,
                text = text,
                bigText = bigText,
                postedAt = postedAt,
                contentHash = hash,
                parseStatus = ParseStatus.FAILED.name,
                matchedRuleId = null,
            ),
        )

        val outcome = parse(settings, monitored.bankKey, title, text, bigText)
        rawNotificationDao.updateParseResult(rawId, outcome.status.name, outcome.matchedRuleId)
        val raw = rawNotificationDao.findById(rawId)?.toDomain()
            ?: RawNotification(
                id = rawId,
                packageName = packageName,
                bankKey = monitored.bankKey,
                title = title,
                text = text,
                bigText = bigText,
                postedAt = postedAt,
                contentHash = hash,
                parseStatus = outcome.status,
                matchedRuleId = outcome.matchedRuleId,
            )

        when (outcome.status) {
            ParseStatus.SUCCESS -> IngestResult.Recorded(raw, insertTransaction(raw, outcome, settings))
            ParseStatus.PARTIAL -> IngestResult.Partial(raw, outcome)
            ParseStatus.FAILED -> IngestResult.Failed(raw, outcome)
        }
    }

    private suspend fun parse(
        settings: AppSettings,
        bankKey: String,
        title: String?,
        text: String?,
        bigText: String?,
    ): ParseOutcome {
        val engine = ParsingEngine(settings.parsing)
        val rules = ruleRepository.activeRulesFor(bankKey)
        val composed = listOfNotNull(title, text, bigText)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .joinToString(separator = " ")
        return engine.parse(bankKey = bankKey, text = composed, rules = rules)
    }

    private suspend fun insertTransaction(
        raw: RawNotification,
        outcome: ParseOutcome,
        settings: AppSettings,
    ): Transaction {
        val window = settings.parsing.dedupeWindowMillis * 3
        val amount = outcome.amountMinor
        val duplicates = if (amount == null) {
            emptyList()
        } else {
            transactionDao.findPossibleDuplicates(
                amount = amount,
                from = raw.postedAt - window,
                to = raw.postedAt + window,
                excludeId = 0L,
            )
        }

        val transaction = Transaction(
            rawNotificationId = raw.id,
            ruleId = outcome.matchedRuleId,
            bankKey = raw.bankKey,
            packageName = raw.packageName,
            type = outcome.type,
            method = outcome.method,
            amountMinor = outcome.amountMinor,
            currency = outcome.currency,
            cardLastDigits = outcome.cardLastDigits,
            counterparty = outcome.counterparty,
            description = outcome.description,
            occurredAt = raw.postedAt,
            parseStatus = outcome.status,
            possibleDuplicate = duplicates.isNotEmpty(),
        )
        val id = transactionDao.insert(transaction.toEntity())
        return transaction.copy(id = id)
    }

    private fun hashOf(vararg parts: String?): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val payload = parts.joinToString(separator = "|") { it.orEmpty() }
        return digest.digest(payload.toByteArray()).joinToString(separator = "") { byte ->
            "%02x".format(byte)
        }
    }

    /** Reapplies the current rules to every stored raw notification. */
    suspend fun reprocessAll(): ReprocessReport = withContext(dispatcher) {
        val settings = settingsRepository.current()
        val engine = ParsingEngine(settings.parsing)
        val raws = rawNotificationDao.getAll()
        var succeeded = 0
        var partial = 0
        var failed = 0

        raws.forEach { entity ->
            val rules = ruleRepository.activeRulesFor(entity.bankKey)
            val text = listOfNotNull(entity.title, entity.text, entity.bigText)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
                .joinToString(separator = " ")
            val outcome = engine.parse(entity.bankKey, text, rules)
            rawNotificationDao.updateParseResult(entity.id, outcome.status.name, outcome.matchedRuleId)

            val existing = transactionDao.findByRawNotification(entity.id)
            when {
                outcome.status == ParseStatus.SUCCESS -> {
                    succeeded++
                    if (existing.isEmpty()) {
                        insertTransaction(entity.toDomain(), outcome, settings)
                    } else {
                        val current = existing.first().toDomain()
                        transactionDao.update(
                            current.copy(
                                ruleId = outcome.matchedRuleId,
                                type = outcome.type,
                                method = outcome.method,
                                amountMinor = outcome.amountMinor,
                                currency = outcome.currency,
                                cardLastDigits = outcome.cardLastDigits,
                                counterparty = outcome.counterparty,
                                description = outcome.description,
                                parseStatus = outcome.status,
                            ).toEntity(),
                        )
                    }
                }

                outcome.status == ParseStatus.PARTIAL -> partial++
                else -> failed++
            }
        }

        ReprocessReport(processed = raws.size, succeeded = succeeded, partial = partial, failed = failed)
    }

    fun observeTransactions(): Flow<List<Transaction>> =
        transactionDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun allTransactions(): List<Transaction> = withContext(dispatcher) {
        transactionDao.getAll().map { it.toDomain() }
    }

    suspend fun updateTransaction(transaction: Transaction) = withContext(dispatcher) {
        transactionDao.update(transaction.copy(isManuallyEdited = true).toEntity())
    }

    suspend fun deleteTransaction(id: Long) = withContext(dispatcher) {
        transactionDao.deleteById(id)
    }

    fun observeRawNotifications(): Flow<List<RawNotification>> =
        rawNotificationDao.observeRecent(limit = 500, offset = 0)
            .map { rows -> rows.map { it.toDomain() } }

    suspend fun rawNotifications(): List<RawNotification> = withContext(dispatcher) {
        rawNotificationDao.getAll().map { it.toDomain() }
    }

    suspend fun clearAllData() = withContext(dispatcher) {
        transactionDao.deleteAll()
        rawNotificationDao.deleteAll()
    }

    /** Used by backup import: inserts brand new transactions. */
    suspend fun importTransactions(transactions: List<Transaction>) = withContext(dispatcher) {
        transactionDao.insertAll(transactions.map { it.toEntity().copy(id = 0L, rawNotificationId = null) })
    }
}
