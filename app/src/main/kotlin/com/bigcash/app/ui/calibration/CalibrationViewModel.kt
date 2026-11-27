package com.bigcash.app.ui.calibration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigcash.app.data.NotificationIngestRepository
import com.bigcash.app.data.PackageRepository
import com.bigcash.app.data.RuleRepository
import com.bigcash.app.data.SettingsRepository
import com.bigcash.core.model.MonitoredPackage
import com.bigcash.core.model.ParseStatus
import com.bigcash.core.model.ParsingRule
import com.bigcash.core.parsing.ParsingEngine
import com.bigcash.core.parsing.RuleMatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CalibrationViewModel @Inject constructor(
    private val ingestRepository: NotificationIngestRepository,
    private val ruleRepository: RuleRepository,
    private val packageRepository: PackageRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val filter = MutableStateFlow(RawFilter())
    private val reprocessing = MutableStateFlow(false)
    private val report = MutableStateFlow<com.bigcash.app.data.ReprocessReport?>(null)

    val playground = MutableStateFlow(PlaygroundState())

    val progress: StateFlow<Pair<Boolean, com.bigcash.app.data.ReprocessReport?>> =
        combine(reprocessing, report) { busy, last -> busy to last }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false to null)

    val state: StateFlow<CalibrationUiState> = ingestRepository.observeRawNotifications()
        .combine(ruleRepository.rules) { raw, rules -> raw to rules }
        .combine(packageRepository.packages) { (raw, rules), packages -> Triple(raw, rules, packages) }
        .combine(filter) { triple, currentFilter -> triple to currentFilter }
        .combine(progress) { (triple, currentFilter), busyAndReport ->
            val (raw, rules, packages) = triple
            val filteredRaw = raw
                .filter { currentFilter.packageName == null || it.packageName == currentFilter.packageName }
                .filter { currentFilter.status == null || it.parseStatus == currentFilter.status }
                .filter {
                    currentFilter.query.isBlank() ||
                        it.fullText.contains(currentFilter.query, ignoreCase = true)
                }
            CalibrationUiState(
                rawNotifications = filteredRaw,
                rules = rules,
                packages = packages,
                rawFilter = currentFilter,
                isReprocessing = busyAndReport.first,
                lastReport = busyAndReport.second,
                failureCount = raw.count { it.parseStatus == ParseStatus.FAILED },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalibrationUiState())

    fun setPackageFilter(packageName: String?) = updateFilter { it.copy(packageName = packageName) }

    fun setStatusFilter(status: ParseStatus?) = updateFilter { it.copy(status = status) }

    fun setQuery(query: String) = updateFilter { it.copy(query = query) }

    fun saveRule(rule: ParsingRule) {
        viewModelScope.launch { ruleRepository.save(rule) }
    }

    fun deleteRule(id: Long) {
        viewModelScope.launch { ruleRepository.delete(id) }
    }

    fun toggleRule(id: Long, active: Boolean) {
        viewModelScope.launch { ruleRepository.setActive(id, active) }
    }

    fun duplicateRule(rule: ParsingRule) {
        viewModelScope.launch { ruleRepository.duplicate(rule) }
    }

    fun changePriority(rule: ParsingRule, delta: Int) {
        val next = (rule.priority + delta).coerceAtLeast(1)
        viewModelScope.launch { ruleRepository.setPriority(rule.id, next) }
    }

    fun validatePattern(type: com.bigcash.core.model.RuleType, pattern: String): String? =
        RuleMatcher.validate(type, pattern)

    fun onPlaygroundText(value: String) {
        playground.value = playground.value.copy(text = value, error = null)
    }

    fun onPlaygroundBank(bankKey: String) {
        playground.value = playground.value.copy(bankKey = bankKey, error = null)
    }

    fun loadIntoPlayground(raw: com.bigcash.core.model.RawNotification) {
        playground.value = playground.value.copy(
            text = raw.fullText,
            bankKey = raw.bankKey,
            error = null,
        )
        runPlayground()
    }

    fun runPlayground() {
        val current = playground.value
        viewModelScope.launch {
            val settings = settingsRepository.current()
            val rules = ruleRepository.activeRulesFor(current.bankKey)
            val engine = ParsingEngine(settings.parsing)
            val outcome = engine.parse(current.bankKey, current.text, rules)
            val traces = engine.trace(current.bankKey, current.text, rules)
            playground.value = current.copy(outcome = outcome, traces = traces)
        }
    }

    fun reprocess() {
        if (reprocessing.value) return
        viewModelScope.launch {
            reprocessing.value = true
            report.value = ingestRepository.reprocessAll()
            reprocessing.value = false
        }
    }

    fun savePackage(pkg: MonitoredPackage) {
        viewModelScope.launch { packageRepository.save(pkg) }
    }

    fun deletePackage(id: Long) {
        viewModelScope.launch { packageRepository.delete(id) }
    }

    private fun updateFilter(transform: (RawFilter) -> RawFilter) {
        filter.value = transform(filter.value)
    }
}
