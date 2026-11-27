package com.bigcash.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.bigcash.app.ui.util.Formatters
import com.bigcash.core.model.PaymentMethod
import com.bigcash.core.model.Transaction
import com.bigcash.core.model.TransactionType

/** Manual correction dialog: the escape hatch when parsing needs a human touch. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionEditorDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDelete: (Long) -> Unit,
) {
    var amount by remember { mutableStateOf(Formatters.amountToInput(transaction.amountMinor)) }
    var type by remember { mutableStateOf(transaction.type) }
    var method by remember { mutableStateOf(transaction.method) }
    var counterparty by remember { mutableStateOf(transaction.counterparty.orEmpty()) }
    var description by remember { mutableStateOf(transaction.description.orEmpty()) }
    var tags by remember { mutableStateOf(transaction.tags.joinToString(", ")) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Edit transaction") },
        confirmButton = {
            TextButton(
                onClick = {
                    val minor = Formatters.inputToMinor(amount)
                    if (amount.isNotBlank() && minor == null) {
                        error = "Amount must look like 12,34"
                    } else {
                        onSave(
                            transaction.copy(
                                amountMinor = minor,
                                type = type,
                                method = method,
                                counterparty = counterparty.ifBlank { null },
                                description = description.ifBlank { null },
                                tags = tags.split(',').map { it.trim() }.filter { it.isNotEmpty() },
                                isManuallyEdited = true,
                            ),
                        )
                        onDismiss()
                    }
                },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GlassTextField(
                    value = amount,
                    onValueChange = { amount = it; error = null },
                    label = "Amount (${transaction.currency})",
                    supportingText = error,
                )
                GlassTextField(
                    value = counterparty,
                    onValueChange = { counterparty = it },
                    label = "Counterparty",
                )
                GlassTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    minLines = 2,
                    singleLine = false,
                )
                GlassTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = "Tags (comma separated)",
                )

                Text("Direction", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionType.entries.forEach { candidate ->
                        GlassTag(
                            text = Formatters.typeLabel(candidate),
                            selected = candidate == type,
                            onClick = { type = candidate },
                            modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                        )
                    }
                }
                Text("Method", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentMethod.entries.forEach { candidate ->
                        GlassTag(
                            text = Formatters.methodLabel(candidate),
                            selected = candidate == method,
                            onClick = { method = candidate },
                            modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                        )
                    }
                }
                TextButton(onClick = {
                    onDelete(transaction.id)
                    onDismiss()
                }) { Text("Delete transaction") }
            }
        },
    )
}
