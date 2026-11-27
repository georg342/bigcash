package com.bigcash.core.database

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase

/** Single Room 3 database for BigCash. Version 1 ships with the first release. */
@Database(
    entities = [
        RawNotificationEntity::class,
        TransactionEntity::class,
        ParsingRuleEntity::class,
        MonitoredPackageEntity::class,
        AppSettingEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ColumnTypeConverters(Converters::class)
abstract class BigCashDatabase : RoomDatabase() {
    abstract fun rawNotificationDao(): RawNotificationDao
    abstract fun transactionDao(): TransactionDao
    abstract fun parsingRuleDao(): ParsingRuleDao
    abstract fun monitoredPackageDao(): MonitoredPackageDao
    abstract fun appSettingDao(): AppSettingDao


    companion object {
        const val NAME = "bigcash.db"
    }
}
