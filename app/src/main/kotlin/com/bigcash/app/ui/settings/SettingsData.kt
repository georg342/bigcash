package com.bigcash.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.bigcash.app.ui.components.GlassSurface
import com.bigcash.app.ui.components.GlassTag

/** Export, import, wipe and onboarding replay. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DataSection(
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit,
    onImport: () -> Unit,
    onClearData: () -> Unit,
    onReplayOnboarding: () -> Unit,
) {
    GlassSurface {
        Text("Data", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Exports land in the app cache and open the share sheet. Imports expect a BigCash JSON backup.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassTag(text = "Export CSV", onClick = onExportCsv)
            GlassTag(text = "Export JSON", onClick = onExportJson)
            GlassTag(text = "Import JSON", onClick = onImport)
            GlassTag(text = "Clear history", onClick = onClearData)
            GlassTag(text = "Replay onboarding", onClick = onReplayOnboarding)
        }
        Column {
            Text(
                text = "Clearing history removes raw notifications and transactions but keeps rules and settings.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
