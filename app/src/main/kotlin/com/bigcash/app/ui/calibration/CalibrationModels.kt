package com.bigcash.app.ui.calibration

import com.bigcash.app.data.ReprocessReport
import com.bigcash.core.model.MonitoredPackage
import com.bigcash.core.model.ParseOutcome
import com.bigcash.core.model.ParseStatus
import com.bigcash.core.model.ParsingRule
import com.bigcash.core.model.RawNotification
import com.bigcash.core.model.RuleTrace

data class RawFilter(
    val packageName: String? = null,
    val status: ParseStatus? = null,
    val query: String = "",
)

data class CalibrationUiState(
    val rawNotifications: List<RawNotification> = emptyList(),
    val rules: List<ParsingRule> = emptyList(),
    val packages: List<MonitoredPackage> = emptyList(),
    val rawFilter: RawFilter = RawFilter(),
    val isReprocessing: Boolean = false,
    val lastReport: ReprocessReport? = null,
    val failureCount: Int = 0,
)

data class PlaygroundState(
    val text: String = "",
    val bankKey: String = "",
    val outcome: ParseOutcome? = null,
    val traces: List<RuleTrace> = emptyList(),
    val error: String? = null,
)
