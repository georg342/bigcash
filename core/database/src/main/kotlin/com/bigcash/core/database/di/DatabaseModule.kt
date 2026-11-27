package com.bigcash.core.database.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.bigcash.core.database.AppSettingDao
import com.bigcash.core.database.BigCashDatabase
import com.bigcash.core.database.MonitoredPackageDao
import com.bigcash.core.database.ParsingRuleDao
import com.bigcash.core.database.RawNotificationDao
import com.bigcash.core.database.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Room 3 always needs an explicit [androidx.sqlite.SQLiteDriver]; the bundled one is used here. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BigCashDatabase =
        Room.databaseBuilder<BigCashDatabase>(
            context = context,
            name = BigCashDatabase.NAME,
        )
            .setDriver(BundledSQLiteDriver())
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideRawNotificationDao(database: BigCashDatabase): RawNotificationDao = database.rawNotificationDao()

    @Provides
    fun provideTransactionDao(database: BigCashDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideParsingRuleDao(database: BigCashDatabase): ParsingRuleDao = database.parsingRuleDao()

    @Provides
    fun provideMonitoredPackageDao(database: BigCashDatabase): MonitoredPackageDao = database.monitoredPackageDao()

    @Provides
    fun provideAppSettingDao(database: BigCashDatabase): AppSettingDao = database.appSettingDao()
}
