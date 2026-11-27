package com.bigcash.app.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigcash.app.ui.components.GlassTag
import com.bigcash.app.ui.components.LiquidBackground
import com.bigcash.app.ui.components.StatTile
import com.bigcash.app.ui.dashboard.DashboardViewModel
import com.bigcash.app.ui.theme.GlassColors
import com.bigcash.app.ui.util.Formatters
import com.bigcash.core.model.TransactionType
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

/** Monthly summary: numbers and ranked lists, no charts. */
@Composable
fun SummaryScreen(contentPadding: PaddingValues = PaddingValues()) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var monthValue by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    val month = YearMonth.parse(monthValue)

    val monthly = state.transactions.filter { transaction ->
        val date = Instant.ofEpochMilli(transaction.occurredAt).atZone(ZoneId.systemDefault()).toLocalDate()
        YearMonth.from(date) == month
    }
    val income = monthly.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor ?: 0L }
    val expenses = monthly.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor ?: 0L }
    val byBank = monthly.groupBy { it.bankKey }
        .mapValues { entry -> Formatters.money(entry.value.sumOf { it.amountMinor ?: 0L }, state.currency) }
    val byMethod = monthly.groupBy { it.method }
        .map { (method, entries) -> Formatters.methodLabel(method) to "${entries.size} entries" }
        .toMap()
    val byTag = monthly.flatMap { transaction -> transaction.tags.map { it to transaction } }
        .groupBy { it.first }
        .mapValues { entry ->
            Formatters.money(entry.value.sumOf { it.second.amountMinor ?: 0L }, state.currency)
        }

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
                    Text("Monthly summary", style = MaterialTheme.typography.displaySmall)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GlassTag(text = "<", onClick = { monthValue = month.minusMonths(1).toString() })
                        GlassTag(text = month.toString())
                        GlassTag(text = ">", onClick = { monthValue = month.plusMonths(1).toString() })
                    }
                    GlassTag(
                        text = "Current month",
                        selected = month == YearMonth.now(),
                        onClick = { monthValue = YearMonth.now().toString() },
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        label = "Income",
                        value = Formatters.money(income, state.currency),
                        accent = GlassColors.Mint,
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        label = "Expenses",
                        value = Formatters.money(expenses, state.currency),
                        accent = GlassColors.Pink,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        label = "Balance",
                        value = Formatters.money(income - expenses, state.currency),
                        accent = GlassColors.Periwinkle,
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        label = "Entries",
                        value = monthly.size.toString(),
                        accent = GlassColors.Amber,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item { SummaryList("By bank", byBank) }
            item { SummaryList("By method", byMethod) }
            if (byTag.isNotEmpty()) {
                item { SummaryList("By tag", byTag) }
            }
        }
    }
}
