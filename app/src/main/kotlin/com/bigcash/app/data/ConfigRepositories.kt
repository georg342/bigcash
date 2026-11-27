package com.bigcash.app.data

import com.bigcash.core.database.MonitoredPackageDao
import com.bigcash.core.database.ParsingRuleDao
import com.bigcash.core.model.MonitoredPackage
import com.bigcash.core.model.ParsingRule
import com.bigcash.core.parsing.ParsingEngine
import com.bigcash.app.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** CRUD for the configurable parsing rules used by the calibration panel. */
@Singleton
class RuleRepository @Inject constructor(
    private val parsingRuleDao: ParsingRuleDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {
    val rules: Flow<List<ParsingRule>> = parsingRuleDao.observeAll()
        .map { rows -> rows.map { it.toDomain() } }
        .flowOn(dispatcher)

    suspend fun activeRulesFor(bankKey: String): List<ParsingRule> = withContext(dispatcher) {
        parsingRuleDao.forBank(bankKey).map { it.toDomain() }
    }

    suspend fun all(): List<ParsingRule> = withContext(dispatcher) {
        parsingRuleDao.getAll().map { it.toDomain() }
    }

    suspend fun save(rule: ParsingRule): Long = withContext(dispatcher) {
        if (rule.id == 0L) {
            parsingRuleDao.insert(rule.toEntity())
        } else {
            parsingRuleDao.update(rule.toEntity())
            rule.id
        }
    }

    suspend fun delete(id: Long) = withContext(dispatcher) { parsingRuleDao.deleteById(id) }

    suspend fun setActive(id: Long, active: Boolean) = withContext(dispatcher) {
        parsingRuleDao.setActive(id, active)
    }

    suspend fun setPriority(id: Long, priority: Int) = withContext(dispatcher) {
        parsingRuleDao.setPriority(id, priority)
    }

    suspend fun duplicate(rule: ParsingRule): Long = save(
        rule.copy(id = 0L, name = "${rule.name} (copy)", priority = rule.priority + 1),
    )

    suspend fun ensureSeeded() = withContext(dispatcher) {
        if (parsingRuleDao.count() == 0) {
            com.bigcash.app.data.seed.DefaultContent.rules().forEach { rule ->
                parsingRuleDao.insert(rule.toEntity())
            }
        }
    }

    companion object {
        const val WILDCARD_BANK: String = ParsingEngine.WILDCARD_BANK
    }
}

/** CRUD for monitored bank packages. */
@Singleton
class PackageRepository @Inject constructor(
    private val monitoredPackageDao: MonitoredPackageDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {
    val packages: Flow<List<MonitoredPackage>> = monitoredPackageDao.observeAll()
        .map { rows -> rows.map { it.toDomain() } }
        .flowOn(dispatcher)

    suspend fun enabledPackage(packageName: String): MonitoredPackage? = withContext(dispatcher) {
        monitoredPackageDao.findByPackage(packageName)?.toDomain()?.takeIf { it.isEnabled }
    }

    suspend fun save(pkg: MonitoredPackage): Long = withContext(dispatcher) {
        if (pkg.id == 0L) {
            monitoredPackageDao.insert(pkg.toEntity())
        } else {
            monitoredPackageDao.update(pkg.toEntity())
            pkg.id
        }
    }

    suspend fun delete(id: Long) = withContext(dispatcher) { monitoredPackageDao.deleteById(id) }

    suspend fun ensureSeeded() = withContext(dispatcher) {
        if (monitoredPackageDao.count() == 0) {
            com.bigcash.app.data.seed.DefaultContent.packages().forEach { pkg ->
                monitoredPackageDao.insert(pkg.toEntity())
            }
        }
    }
}
