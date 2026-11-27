package com.bigcash.app.ui.calibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import com.bigcash.app.ui.components.GlassTag
import com.bigcash.app.ui.components.GlassTextField
import com.bigcash.core.model.ParsingRule
import com.bigcash.core.model.PaymentMethod
import com.bigcash.core.model.RuleType
import com.bigcash.core.model.TransactionType
import com.bigcash.core.parsing.RuleMatcher

/** Create/edit dialog for a parsing rule. Every field the engine reads is exposed. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RuleEditorDialog(
    rule: ParsingRule,
    banks: List<String>,
    onDismiss: () -> Unit,
    onSave: (ParsingRule) -> Unit,
) {
    var name by remember { mutableStateOf(rule.name) }
    var bankKey by remember { mutableStateOf(rule.bankKey) }
    var type by remember { mutableStateOf(rule.type) }
    var pattern by remember { mutableStateOf(rule.pattern) }
    var priority by remember { mutableStateOf(rule.priority.toString()) }
    var amountGroup by remember { mutableStateOf(rule.amountGroup?.toString().orEmpty()) }
    var counterpartyGroup by remember { mutableStateOf(rule.counterpartyGroup?.toString().orEmpty()) }
    var cardGroup by remember { mutableStateOf(rule.cardGroup?.toString().orEmpty()) }
    var typeValue by remember { mutableStateOf(rule.typeValue.orEmpty()) }
    var methodValue by remember { mutableStateOf(rule.methodValue.orEmpty()) }
    var currencyValue by remember { mutableStateOf(rule.currencyValue.orEmpty()) }
    var description by remember { mutableStateOf(rule.description) }
    var active by remember { mutableStateOf(rule.isActive) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (rule.id == 0L) "New rule" else "Edit rule") },
        confirmButton = {
            TextButton(onClick = {
                val validation = RuleMatcher.validate(type, pattern)
                if (name.isBlank()) {
                    error = "Name is required"
                } else if (validation != null) {
                    error = validation
                } else {
                    onSave(
                        rule.copy(
                            name = name,
                            bankKey = bankKey,
                            type = type,
                            pattern = pattern,
                            priority = priority.toIntOrNull() ?: 100,
                            amountGroup = amountGroup.toIntOrNull(),
                            counterpartyGroup = counterpartyGroup.toIntOrNull(),
                            cardGroup = cardGroup.toIntOrNull(),
                            typeValue = typeValue.ifBlank { null },
                            methodValue = methodValue.ifBlank { null },
                            currencyValue = currencyValue.ifBlank { null },
                            description = description,
                            isActive = active,
                        ),
                    )
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GlassTextField(name, { name = it }, "Rule name")
                GlassTextField(pattern, { pattern = it }, "Pattern", minLines = 3, singleLine = false)
                Text(
                    text = when (type) {
                        RuleType.REGEX -> "Full regular expression. Capture groups are referenced below."
                        RuleType.MASK -> "Mask: # is a digit, * is any run of characters."
                        RuleType.KEYWORD -> "Comma separated keywords, first hit wins."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("Type", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RuleType.entries.forEach { candidate ->
                        GlassTag(
                            text = candidate.name,
                            selected = candidate == type,
                            onClick = { type = candidate },
                        )
                    }
                }
                Text("Bank", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    banks.distinct().forEach { candidate ->
                        GlassTag(
                            text = if (candidate == "*") "Any bank" else candidate,
                            selected = candidate == bankKey,
                            onClick = { bankKey = candidate },
                        )
                    }
                }
                GlassTextField(priority, { priority = it.filter(Char::isDigit) }, "Priority (1 runs first)")
                GlassTextField(amountGroup, { amountGroup = it.filter(Char::isDigit) }, "Amount group")
                GlassTextField(cardGroup, { cardGroup = it.filter(Char::isDigit) }, "Card digits group")
                GlassTextField(
                    counterpartyGroup,
                    { counterpartyGroup = it.filter(Char::isDigit) },
                    "Counterparty group",
                )
                Text("Direction override", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("", TransactionType.INCOME.name, TransactionType.EXPENSE.name).forEach { option ->
                        GlassTag(
                            text = option.ifBlank { "Auto" },
                            selected = option == typeValue,
                            onClick = { typeValue = option },
                        )
                    }
                }
                Text("Method override", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (listOf("") + PaymentMethod.entries.map { it.name }).forEach { option ->
                        GlassTag(
                            text = if (option.isBlank()) "Auto" else option,
                            selected = option == methodValue,
                            onClick = { methodValue = option },
                        )
                    }
                }
                GlassTextField(currencyValue, { currencyValue = it }, "Currency override")
                GlassTextField(
                    description,
                    { description = it },
                    "Description",
                    minLines = 2,
                    singleLine = false,
                )
                GlassTag(
                    text = if (active) "Active" else "Disabled",
                    selected = active,
                    onClick = { active = !active },
                )
                error?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
    )
}
