package com.bigcash.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigcash.app.ui.components.GlassSurface
import com.bigcash.app.ui.components.GlassTag
import com.bigcash.app.ui.components.GlassTextField
import com.bigcash.app.ui.util.Formatters
import com.bigcash.core.model.ParseStatus
import com.bigcash.core.model.PaymentMethod
import com.bigcash.core.model.TransactionType

/** All combinable dashboard filters in one glass panel. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardFilters(
    state: DashboardUiState,
    onBankToggle: (String) -> Unit,
    onTypeSelect: (TransactionType?) -> Unit,
    onMethodSelect: (PaymentMethod?) -> Unit,
    onPeriodSelect: (PeriodFilter) -> Unit,
    onStatusToggle: (ParseStatus) -> Unit,
    onTagToggle: (String) -> Unit,
    onAmountRange: (Long?, Long?) -> Unit,
    onDuplicatesToggle: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val filters = state.filters
    var minInput by rememberSaveable { mutableStateOf("") }
    var maxInput by rememberSaveable { mutableStateOf("") }

    GlassSurface(modifier = modifier.fillMaxWidth()) {
        ChipGroup(
            title = "Period",
            items = PeriodFilter.entries,
            isSelected = { it == filters.period },
            label = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
            onToggle = onPeriodSelect,
        )
        if (state.availableBanks.isNotEmpty()) {
            ChipGroup(
                title = "Bank",
                items = state.availableBanks,
                isSelected = filters.bankKeys::contains,
                label = Formatters::bankLabel,
                onToggle = onBankToggle,
            )
        }
        ChipGroup(
            title = "Direction",
            items = listOf<TransactionType?>(null, TransactionType.INCOME, TransactionType.EXPENSE),
            isSelected = { it == filters.type },
            label = { if (it == null) "All" else Formatters.typeLabel(it) },
            onToggle = onTypeSelect,
        )
        ChipGroup(
            title = "Method",
            items = listOf<PaymentMethod?>(null) + PaymentMethod.entries.filter { it != PaymentMethod.UNKNOWN },
            isSelected = { it == filters.method },
            label = { if (it == null) "All" else Formatters.methodLabel(it) },
            onToggle = onMethodSelect,
        )
        ChipGroup(
            title = "Parse status",
            items = ParseStatus.entries,
            isSelected = filters.statuses::contains,
            label = Formatters::statusLabel,
            onToggle = onStatusToggle,
        )
        if (state.availableTags.isNotEmpty()) {
            ChipGroup(
                title = "Tags",
                items = state.availableTags,
                isSelected = filters.tags::contains,
                label = { "#$it" },
                onToggle = onTagToggle,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GlassTextField(
                value = minInput,
                onValueChange = {
                    minInput = it
                    onAmountRange(Formatters.inputToMinor(it), filters.maxMinor)
                },
                label = "Min amount",
                modifier = Modifier.weight(1f),
            )
            GlassTextField(
                value = maxInput,
                onValueChange = {
                    maxInput = it
                    onAmountRange(filters.minMinor, Formatters.inputToMinor(it))
                },
                label = "Max amount",
                modifier = Modifier.weight(1f),
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GlassTag(
                text = "Possible duplicates",
                selected = filters.duplicatesOnly,
                onClick = onDuplicatesToggle,
            )
            GlassTag(text = "Reset filters", onClick = onClear)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipGroup(
    title: String,
    items: List<T>,
    isSelected: (T) -> Boolean,
    label: (T) -> String,
    onToggle: (T) -> Unit,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            GlassTag(
                text = label(item),
                selected = isSelected(item),
                onClick = { onToggle(item) },
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
    }
}
