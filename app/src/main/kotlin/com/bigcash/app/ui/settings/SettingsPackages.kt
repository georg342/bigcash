package com.bigcash.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.bigcash.app.ui.components.GlassSurface
import com.bigcash.app.ui.components.GlassTag
import com.bigcash.app.ui.components.GlassTextField
import com.bigcash.core.model.MonitoredPackage

/** Monitored package CRUD, fully user managed. */
@Composable
fun PackageSection(
    packages: List<MonitoredPackage>,
    onSave: (MonitoredPackage) -> Unit,
    onDelete: (Long) -> Unit,
) {
    var editing by remember { mutableStateOf<MonitoredPackage?>(null) }
    var creating by remember { mutableStateOf(false) }

    GlassSurface {
        Row {
            Text(
                text = "Monitored packages",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            GlassTag(text = "Add", onClick = { creating = true })
        }
        packages.forEach { pkg ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(pkg.displayName, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "${pkg.packageName} \u2022 ${pkg.bankKey}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                GlassTag(
                    text = if (pkg.isEnabled) "On" else "Off",
                    selected = pkg.isEnabled,
                    onClick = { onSave(pkg.copy(isEnabled = !pkg.isEnabled)) },
                )
                GlassTag(
                    text = "Edit",
                    onClick = { editing = pkg },
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        if (packages.isEmpty()) {
            Text(
                text = "No packages yet. Add the bank apps you want to monitor.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }

    if (creating) {
        PackageEditorDialog(
            pkg = MonitoredPackage(packageName = "", bankKey = "", displayName = ""),
            onDismiss = { creating = false },
            onSave = { pkg ->
                onSave(pkg)
                creating = false
            },
            onDelete = {},
        )
    }

    editing?.let { pkg ->
        PackageEditorDialog(
            pkg = pkg,
            onDismiss = { editing = null },
            onSave = { updated ->
                onSave(updated)
                editing = null
            },
            onDelete = {
                onDelete(pkg.id)
                editing = null
            },
        )
    }
}

@Composable
private fun PackageEditorDialog(
    pkg: MonitoredPackage,
    onDismiss: () -> Unit,
    onSave: (MonitoredPackage) -> Unit,
    onDelete: () -> Unit,
) {
    var packageName by remember { mutableStateOf(pkg.packageName) }
    var bankKey by remember { mutableStateOf(pkg.bankKey) }
    var displayName by remember { mutableStateOf(pkg.displayName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (pkg.id == 0L) "Add package" else "Edit package") },
        confirmButton = {
            TextButton(
                onClick = {
                    if (packageName.isNotBlank() && bankKey.isNotBlank()) {
                        onSave(
                            pkg.copy(
                                packageName = packageName.trim(),
                                bankKey = bankKey.trim().lowercase(),
                                displayName = displayName.ifBlank { bankKey.trim() },
                            ),
                        )
                    }
                },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassTextField(displayName, { displayName = it }, "Display name")
                GlassTextField(packageName, { packageName = it }, "Package name")
                GlassTextField(bankKey, { bankKey = it }, "Bank key (rules match on this)")
                if (pkg.id != 0L) {
                    TextButton(onClick = onDelete) { Text("Remove package") }
                }
            }
        },
    )
}
