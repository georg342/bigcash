package com.bigcash.app.data

import com.bigcash.core.model.ParseStatus
import com.bigcash.core.model.PaymentMethod
import com.bigcash.core.model.Transaction
import com.bigcash.core.model.TransactionType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/** Result of importing a backup file. */
data class ImportReport(val imported: Int, val skipped: Int)

/** Restores transactions from an exported JSON backup. */
@Singleton
class BackupImporter @Inject constructor(
    private val ingestRepository: NotificationIngestRepository,
) {

    suspend fun importJson(content: String): ImportReport = withContext(Dispatchers.IO) {
        val array = JSONArray(content)
        val imported = mutableListOf<Transaction>()
        var skipped = 0

        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index)
            val occurredAt = item?.optLong("occurredAt", 0L) ?: 0L
            if (item == null || occurredAt <= 0L) {
                skipped++
                continue
            }
            val amount = if (item.isNull("amountMinor")) null else item.optLong("amountMinor")
            val tags = item.optJSONArray("tags")?.let { tagsArray ->
                (0 until tagsArray.length()).map { tagsArray.optString(it) }
            }.orEmpty()
            imported += Transaction(
                rawNotificationId = null,
                ruleId = null,
                bankKey = item.optString("bankKey", "imported"),
                packageName = item.optString("packageName", "imported"),
                type = TransactionType.fromKey(item.optString("type")),
                method = PaymentMethod.fromKey(item.optString("method")),
                amountMinor = amount,
                currency = item.optString("currency", "BRL"),
                cardLastDigits = item.optString("cardLastDigits").takeIf { it.isNotBlank() },
                counterparty = item.optString("counterparty").takeIf { it.isNotBlank() },
                description = item.optString("description").takeIf { it.isNotBlank() },
                occurredAt = occurredAt,
                parseStatus = ParseStatus.fromKey(item.optString("parseStatus")),
                tags = tags,
                possibleDuplicate = item.optBoolean("possibleDuplicate", false),
            )
        }

        ingestRepository.importTransactions(imported)
        ImportReport(imported = imported.size, skipped = skipped)
    }
}
