package com.bigcash.app.ui.summary

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigcash.app.ui.components.GlassSurface

/** Simple ranked list used by the monthly summary. */
@Composable
fun SummaryList(title: String, values: Map<String, String>) {
    GlassSurface {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (values.isEmpty()) {
            Text(
                text = "Nothing in this period",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        values.entries.sortedByDescending { it.value }.forEach { entry ->
            Row(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = entry.key,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = entry.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
