package com.bigcash.app.data

import com.bigcash.app.data.model.AppSettings
import com.bigcash.app.data.seed.DefaultContent
import com.bigcash.core.database.AppSettingDao
import com.bigcash.core.parsing.ParsingEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Single source of truth for every configurable parameter. */
@Singleton
class SettingsRepository @Inject constructor(
    private val appSettingDao: AppSettingDao,
    @com.bigcash.app.di.IoDispatcher private val dispatcher: CoroutineDispatcher,
) {
    private val defaults: AppSettings = DefaultContent.settings()

    val settings: Flow<AppSettings> = appSettingDao.observeAll()
        .map { rows -> SettingsCodec.fromRows(defaults, rows) }
        .flowOn(dispatcher)

    suspend fun current(): AppSettings = withContext(dispatcher) {
        SettingsCodec.fromRows(defaults, appSettingDao.getAll())
    }

    suspend fun save(settings: AppSettings) = withContext(dispatcher) {
        appSettingDao.upsertAll(SettingsCodec.toRows(settings))
    }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        val updated = transform(current())
        save(updated)
    }

    /** Creates the settings rows on first launch so the calibration panel has real values. */
    suspend fun ensureSeeded() = withContext(dispatcher) {
        if (appSettingDao.getAll().isEmpty()) {
            appSettingDao.upsertAll(SettingsCodec.toRows(defaults))
        }
    }

    suspend fun engine(): ParsingEngine = ParsingEngine(current().parsing)

    suspend fun awaitSettings(): AppSettings = settings.first()
}
