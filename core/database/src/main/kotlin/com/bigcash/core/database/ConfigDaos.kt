package com.bigcash.core.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

/** Calibration panel CRUD for parsing rules. */
@Dao
interface ParsingRuleDao {

    @Query("SELECT * FROM parsing_rules ORDER BY priority ASC, id ASC")
    fun observeAll(): Flow<List<ParsingRuleEntity>>

    @Query("SELECT * FROM parsing_rules WHERE bankKey = :bankKey OR bankKey = '*' ORDER BY priority ASC, id ASC")
    suspend fun forBank(bankKey: String): List<ParsingRuleEntity>

    @Query("SELECT * FROM parsing_rules ORDER BY priority ASC, id ASC")
    suspend fun getAll(): List<ParsingRuleEntity>

    @Insert
    suspend fun insert(entity: ParsingRuleEntity): Long

    @Update
    suspend fun update(entity: ParsingRuleEntity)

    @Query("DELETE FROM parsing_rules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE parsing_rules SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)

    @Query("UPDATE parsing_rules SET priority = :priority WHERE id = :id")
    suspend fun setPriority(id: Long, priority: Int)

    @Query("SELECT COUNT(*) FROM parsing_rules")
    suspend fun count(): Int

    @Query("DELETE FROM parsing_rules")
    suspend fun deleteAll()
}

/** Monitored bank apps. */
@Dao
interface MonitoredPackageDao {

    @Query("SELECT * FROM monitored_packages ORDER BY displayName ASC")
    fun observeAll(): Flow<List<MonitoredPackageEntity>>

    @Query("SELECT * FROM monitored_packages WHERE isEnabled = 1")
    suspend fun enabledPackages(): List<MonitoredPackageEntity>

    @Query("SELECT * FROM monitored_packages ORDER BY displayName ASC")
    suspend fun getAll(): List<MonitoredPackageEntity>

    @Query("SELECT * FROM monitored_packages WHERE packageName = :packageName LIMIT 1")
    suspend fun findByPackage(packageName: String): MonitoredPackageEntity?

    @Insert
    suspend fun insert(entity: MonitoredPackageEntity): Long

    @Update
    suspend fun update(entity: MonitoredPackageEntity)

    @Query("DELETE FROM monitored_packages WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM monitored_packages")
    suspend fun count(): Int
}

/** Every configurable parameter lives here as a key/value pair. */
@Dao
interface AppSettingDao {

    @Query("SELECT * FROM app_settings")
    fun observeAll(): Flow<List<AppSettingEntity>>

    @Query("SELECT * FROM app_settings")
    suspend fun getAll(): List<AppSettingEntity>

    @Query("SELECT value FROM app_settings WHERE key = :key")
    suspend fun get(key: String): String?

    @Upsert
    suspend fun upsert(entity: AppSettingEntity)

    @Upsert
    suspend fun upsertAll(entities: List<AppSettingEntity>)

    @Query("DELETE FROM app_settings")
    suspend fun deleteAll()
}
