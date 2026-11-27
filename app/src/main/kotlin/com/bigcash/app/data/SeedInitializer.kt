package com.bigcash.app.data

import javax.inject.Inject
import javax.inject.Singleton

/** Fills the database with editable defaults the first time the app runs. */
@Singleton
class SeedInitializer @Inject constructor(
    private val packageRepository: PackageRepository,
    private val ruleRepository: RuleRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun seed() {
        settingsRepository.ensureSeeded()
        packageRepository.ensureSeeded()
        ruleRepository.ensureSeeded()
    }
}
