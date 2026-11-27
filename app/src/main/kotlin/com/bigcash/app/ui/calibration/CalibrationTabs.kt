package com.bigcash.app.ui.calibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigcash.app.ui.components.EmptyState
import com.bigcash.app.ui.components.GlassSurface
import com.bigcash.app.ui.components.GlassTag
import com.bigcash.app.ui.components.GlassTextField
import com.bigcash.app.ui.theme.GlassColors
import com.bigcash.app.ui.util.Formatters
import com.bigcash.core.model.ParseStatus
import com.bigcash.core.model.RawNotification

/** Raw notification history with package/status filters. */
internal fun LazyListScope.rawTab(state: CalibrationUiState, viewModel: CalibrationViewModel) {
    item { RawFilters(state, viewModel) }
    if (state.rawNotifications.isEmpty()) {
        item {
            EmptyState(
                title = "No raw notifications",
                message = "Grant notification access and wait for the next bank notification.",
            )
        }
    } else {
        items(state.rawNotifications, key = { it.id }) { notification ->
            RawNotificationCard(notification, onUse = viewModel::loadIntoPlayground)
        }
    }
}

/** Only the notifications that matched no rule, ready to become new rules. */
internal fun LazyListScope.failuresTab(state: CalibrationUiState, viewModel: CalibrationViewModel) {
    val failures = state.rawNotifications.filter { it.parseStatus == ParseStatus.FAILED }
    item {
        Text(
            text = "${failures.size} notifications matched no rule",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (failures.isEmpty()) {
        item {
            EmptyState(
                title = "No parse failures",
                message = "Every captured notification produced at least a partial parse.",
            )
        }
    } else {
        items(failures, key = { it.id }) { notification ->
            RawNotificationCard(notification, onUse = viewModel::loadIntoPlayground)
        }
    }
}

@Composable
private fun RawFilters(state: CalibrationUiState, viewModel: CalibrationViewModel) {
    var query by rememberSaveable { mutableStateOf(state.rawFilter.query) }
    GlassSurface {
        Text("Package", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassTag(
                text = "All",
                selected = state.rawFilter.packageName == null,
                onClick = { viewModel.setPackageFilter(null) },
            )
            state.packages.forEach { pkg ->
                GlassTag(
                    text = pkg.displayName,
                    selected = state.rawFilter.packageName == pkg.packageName,
                    onClick = { viewModel.setPackageFilter(pkg.packageName) },
                )
            }
        }
        Text(
            text = "Parse status",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 10.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassTag(
                text = "All",
                selected = state.rawFilter.status == null,
                onClick = { viewModel.setStatusFilter(null) },
            )
            ParseStatus.entries.forEach { status ->
                GlassTag(
                    text = Formatters.statusLabel(status),
                    selected = state.rawFilter.status == status,
                    onClick = {
                        viewModel.setStatusFilter(if (state.rawFilter.status == status) null else status)
                    },
                )
            }
        }
        GlassTextField(
            value = query,
            onValueChange = {
                query = it
                viewModel.setQuery(it)
            },
            label = "Search raw text",
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

@Composable
private fun RawNotificationCard(notification: RawNotification, onUse: (RawNotification) -> Unit) {
    GlassSurface(modifier = Modifier.fillMaxWidth(), onClick = { onUse(notification) }) {
        Row {
            Text(
                text = Formatters.bankLabel(notification.bankKey),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = Formatters.statusLabel(notification.parseStatus),
                style = MaterialTheme.typography.labelSmall,
                color = when (notification.parseStatus) {
                    ParseStatus.SUCCESS -> GlassColors.Mint
                    ParseStatus.PARTIAL -> GlassColors.Amber
                    ParseStatus.FAILED -> GlassColors.Pink
                },
            )
        }
        Text(
            text = notification.fullText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = Formatters.dateTime(notification.postedAt),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
