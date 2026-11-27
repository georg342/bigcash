package com.bigcash.app.ui.calibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigcash.app.ui.components.GlassAction
import com.bigcash.app.ui.components.GlassTag
import com.bigcash.app.ui.components.LiquidBackground
import com.bigcash.app.ui.theme.GlassColors

private enum class CalibrationTab(val label: String) {
    RAW("Raw"),
    RULES("Rules"),
    PLAYGROUND("Playground"),
    FAILURES("Failures"),
}

/** Calibration panel: raw history, rule CRUD, playground and parse failures. */
@Composable
fun CalibrationScreen(
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: CalibrationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val playground by viewModel.playground.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(CalibrationTab.RAW) }

    LiquidBackground(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 40.dp,
                bottom = contentPadding.calculateBottomPadding() + 40.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Calibration", style = MaterialTheme.typography.displaySmall)
                    Text(
                        text = "${state.rules.size} rules \u2022 ${state.rawNotifications.size} raw notifications " +
                            "\u2022 ${state.failureCount} failures",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CalibrationTab.entries.forEach { entry ->
                            GlassTag(
                                text = entry.label,
                                selected = tab == entry,
                                onClick = { tab = entry },
                            )
                        }
                    }
                    GlassAction(
                        text = if (state.isReprocessing) "Reprocessing..." else "Reprocess history",
                        enabled = !state.isReprocessing,
                        onClick = viewModel::reprocess,
                    )
                    state.lastReport?.let { report ->
                        Text(
                            text = "Processed ${report.processed} \u2022 ${report.succeeded} ok \u2022 " +
                                "${report.partial} partial \u2022 ${report.failed} failed",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlassColors.Mint,
                        )
                    }
                }
            }

            when (tab) {
                CalibrationTab.RAW -> rawTab(state, viewModel)
                CalibrationTab.RULES -> rulesTab(state, viewModel)
                CalibrationTab.PLAYGROUND -> playgroundTab(state, playground, viewModel)
                CalibrationTab.FAILURES -> failuresTab(state, viewModel)
            }
        }
    }
}
