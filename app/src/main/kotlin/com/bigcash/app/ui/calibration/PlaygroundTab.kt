package com.bigcash.app.ui.calibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigcash.app.ui.components.GlassAction
import com.bigcash.app.ui.components.GlassSurface
import com.bigcash.app.ui.components.GlassTag
import com.bigcash.app.ui.components.GlassTextField
import com.bigcash.app.ui.theme.GlassColors
import com.bigcash.app.ui.util.Formatters
import com.bigcash.core.model.ParseStatus

/** Paste a real notification and see exactly what each rule extracts. */
internal fun LazyListScope.playgroundTab(
    state: CalibrationUiState,
    playground: PlaygroundState,
    viewModel: CalibrationViewModel,
) {
    item { PlaygroundContent(state, playground, viewModel) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlaygroundContent(
    state: CalibrationUiState,
    playground: PlaygroundState,
    viewModel: CalibrationViewModel,
) {
    val banks = (state.packages.map { it.bankKey } + "*").distinct()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassSurface {
            Text("Bank", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                banks.forEach { bank ->
                    GlassTag(
                        text = if (bank == "*") "Any bank" else Formatters.bankLabel(bank),
                        selected = playground.bankKey == bank,
                        onClick = { viewModel.onPlaygroundBank(bank) },
                    )
                }
            }
            GlassTextField(
                value = playground.text,
                onValueChange = viewModel::onPlaygroundText,
                label = "Notification text",
                minLines = 4,
                singleLine = false,
                modifier = Modifier.padding(top = 10.dp),
            )
            GlassAction(
                text = "Run rules",
                onClick = viewModel::runPlayground,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        playground.outcome?.let { outcome ->
            GlassSurface {
                Row {
                    Text(
                        text = Formatters.statusLabel(outcome.status),
                        style = MaterialTheme.typography.titleMedium,
                        color = when (outcome.status) {
                            ParseStatus.SUCCESS -> GlassColors.Mint
                            ParseStatus.PARTIAL -> GlassColors.Amber
                            ParseStatus.FAILED -> GlassColors.Pink
                        },
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = Formatters.money(outcome.amountMinor, outcome.currency),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(
                    text = "Winner: rule ${outcome.matchedRuleId ?: "none"} \u2022 " +
                        "${Formatters.typeLabel(outcome.type)} \u2022 ${Formatters.methodLabel(outcome.method)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
                outcome.cardLastDigits?.let { digits ->
                    Text(
                        text = "Card $digits",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (playground.traces.isNotEmpty()) {
            GlassSurface {
                Text("Rule trace", style = MaterialTheme.typography.titleMedium)
                playground.traces.forEach { trace ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    ) {
                        Text(
                            text = "P${trace.priority} ${trace.ruleName}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = if (trace.matched) {
                                trace.amountMinor?.let { Formatters.money(it) } ?: "matched"
                            } else {
                                "no match"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (trace.matched) GlassColors.Mint else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (trace.matchedText != null) {
                        Text(
                            text = "\u2192 ${trace.matchedText}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
