package com.bigcash.app.ui.calibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigcash.app.ui.components.GlassAction
import com.bigcash.app.ui.components.GlassSurface
import com.bigcash.app.ui.components.GlassTag
import com.bigcash.app.ui.theme.GlassColors
import com.bigcash.app.ui.util.Formatters
import com.bigcash.core.model.ParsingRule
import com.bigcash.core.model.RuleType

/** Rule list with inline CRUD controls. */
internal fun LazyListScope.rulesTab(state: CalibrationUiState, viewModel: CalibrationViewModel) {
    item { RulesTabContent(state, viewModel) }
}

@Composable
private fun RulesTabContent(state: CalibrationUiState, viewModel: CalibrationViewModel) {
    var editing by remember { mutableStateOf<ParsingRule?>(null) }
    var creating by remember { mutableStateOf(false) }
    val banks = state.packages.map { it.bankKey }.distinct().ifEmpty { listOf("*") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassAction(text = "New rule", onClick = { creating = true })

        state.rules.forEach { rule ->
            GlassSurface(modifier = Modifier.fillMaxWidth(), onClick = { editing = rule }) {
                Row {
                    Text(
                        text = rule.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "P${rule.priority}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GlassColors.Mint,
                    )
                }
                Text(
                    text = "${Formatters.bankLabel(rule.bankKey)} \u2022 ${rule.type.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = rule.pattern,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp),
                )
                if (rule.description.isNotBlank()) {
                    Text(
                        text = rule.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GlassTag(
                        text = if (rule.isActive) "Active" else "Disabled",
                        selected = rule.isActive,
                        onClick = { viewModel.toggleRule(rule.id, !rule.isActive) },
                    )
                    GlassTag(text = "Up", onClick = { viewModel.changePriority(rule, -1) })
                    GlassTag(text = "Down", onClick = { viewModel.changePriority(rule, 1) })
                    GlassTag(text = "Duplicate", onClick = { viewModel.duplicateRule(rule) })
                    GlassTag(text = "Delete", onClick = { viewModel.deleteRule(rule.id) })
                }
            }
        }

        TextButton(onClick = { creating = true }) { Text("Create the first rule") }

        if (creating) {
            RuleEditorDialog(
                rule = ParsingRule(
                    name = "",
                    bankKey = banks.first(),
                    type = RuleType.REGEX,
                    pattern = "",
                    priority = (state.rules.maxOfOrNull { it.priority } ?: 0) + 10,
                ),
                banks = banks + "*",
                onDismiss = { creating = false },
                onSave = { rule ->
                    viewModel.saveRule(rule)
                    creating = false
                },
            )
        }

        editing?.let { rule ->
            RuleEditorDialog(
                rule = rule,
                banks = banks + "*",
                onDismiss = { editing = null },
                onSave = { updated ->
                    viewModel.saveRule(updated)
                    editing = null
                },
            )
        }
    }
}
