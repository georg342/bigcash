package com.bigcash.app.ui.settings

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigcash.app.data.BackupImporter
import com.bigcash.app.data.BackupService
import com.bigcash.app.data.NotificationIngestRepository
import com.bigcash.app.data.PackageRepository
import com.bigcash.app.data.SettingsRepository
import com.bigcash.app.data.model.AppSettings
import com.bigcash.core.model.MonitoredPackage
import com.bigcash.core.model.ParsingConfig
import com.bigcash.core.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val packages: List<MonitoredPackage> = emptyList(),
    val message: String? = null,
    val share: Intent? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val packageRepository: PackageRepository,
    private val backupService: BackupService,
    private val backupImporter: BackupImporter,
    private val ingestRepository: NotificationIngestRepository,
) : ViewModel() {

    private val message = MutableStateFlow<String?>(null)
    private val share = MutableStateFlow<Intent?>(null)

    val state: StateFlow<SettingsUiState> = settingsRepository.settings
        .combine(packageRepository.packages) { settings, packages -> settings to packages }
        .combine(message) { (settings, packages), currentMessage -> Triple(settings, packages, currentMessage) }
        .combine(share) { triple, currentShare ->
            SettingsUiState(
                settings = triple.first,
                packages = triple.second,
                message = triple.third,
                share = currentShare,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setTheme(mode: ThemeMode) = updateSettings { it.copy(themeMode = mode) }

    fun setNotificationsEnabled(enabled: Boolean) = updateSettings {
        it.copy(notificationsEnabled = enabled, appNotification = it.appNotification.copy(enabled = enabled))
    }

    fun setSuccessTemplate(template: String) = updateSettings { it.copy(successTemplate = template) }

    fun setFailureTemplate(template: String) = updateSettings { it.copy(failureTemplate = template) }

    fun completeOnboarding() = updateSettings { it.copy(onboardingCompleted = true) }

    fun resetOnboarding() = updateSettings { it.copy(onboardingCompleted = false) }

    fun updateParsing(transform: (ParsingConfig) -> ParsingConfig) = updateSettings { current ->
        current.copy(parsing = transform(current.parsing))
    }

    fun savePackage(pkg: MonitoredPackage) {
        viewModelScope.launch {
            packageRepository.save(pkg)
            message.value = "Package saved"
        }
    }

    fun deletePackage(id: Long) {
        viewModelScope.launch {
            packageRepository.delete(id)
            message.value = "Package removed"
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            val file = backupService.exportCsv()
            share.value = backupService.shareIntent(file, "text/csv")
            message.value = "CSV exported to ${file.name}"
        }
    }

    fun exportJson() {
        viewModelScope.launch {
            val file = backupService.exportJson()
            share.value = backupService.shareIntent(file, "application/json")
            message.value = "Backup exported to ${file.name}"
        }
    }

    fun importJson(content: String) {
        viewModelScope.launch {
            val report = backupImporter.importJson(content)
            message.value = "Imported ${report.imported} transactions (${report.skipped} skipped)"
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            ingestRepository.clearAllData()
            message.value = "Local history cleared"
        }
    }

    fun consumeShare() {
        share.value = null
    }

    fun consumeMessage() {
        message.value = null
    }

    private fun updateSettings(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { settingsRepository.update(transform) }
    }
}
