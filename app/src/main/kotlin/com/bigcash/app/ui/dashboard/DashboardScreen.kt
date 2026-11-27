package com.bigcash.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigcash.app.notification.NotificationAccess
import com.bigcash.app.ui.components.EmptyState
import com.bigcash.app.ui.components.GlassTag
import com.bigcash.app.ui.components.GlassTextField
import com.bigcash.app.ui.components.LiquidBackground
import com.bigcash.app.ui.components.StatTile
import com.bigcash.app.ui.theme.GlassColors
import com.bigcash.app.ui.util.Formatters
import com.bigcash.core.model.Transaction

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var editing by remember { mutableStateOf<Transaction?>(null) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val accessGranted = remember(context) { NotificationAccess.isGranted(context) }

    LiquidBackground(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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
                    Text("BigCash", style = MaterialTheme.typography.displaySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassTag(
                            text = if (accessGranted) "Listener active" else "Listener disabled",
                            selected = accessGranted,
                        )
                        GlassTag(
                            text = if (showFilters) "Hide filters" else "Filters",
                            selected = showFilters,
                            onClick = { showFilters = !showFilters },
                        )
                    }
                    GlassTextField(
                        value = state.filters.query,
                        onValueChange = viewModel::onQueryChange,
                        label = "Search",
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        label = "Income",
                        value = Formatters.money(state.totalIncomeMinor, state.currency),
                        accent = GlassColors.Mint,
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        label = "Expenses",
                        value = Formatters.money(state.totalExpenseMinor, state.currency),
                        accent = GlassColors.Pink,
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        label = "Balance",
                        value = Formatters.money(
                            state.totalIncomeMinor - state.totalExpenseMinor,
                            state.currency,
                        ),
                        accent = GlassColors.Periwinkle,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (showFilters) {
                item {
                    DashboardFilters(
                        state = state,
                        onBankToggle = viewModel::toggleBank,
                        onTypeSelect = viewModel::setType,
                        onMethodSelect = viewModel::setMethod,
                        onPeriodSelect = viewModel::setPeriod,
                        onStatusToggle = viewModel::toggleStatus,
                        onTagToggle = viewModel::toggleTag,
                        onAmountRange = viewModel::setAmountRange,
                        onDuplicatesToggle = viewModel::toggleDuplicatesOnly,
                        onClear = viewModel::clearFilters,
                    )
                }
            }

            if (state.transactions.isEmpty()) {
                item {
                    EmptyState(
                        title = "No transactions in this view",
                        message = "Capture a bank notification or relax the filters.",
                    )
                }
            } else {
                items(state.transactions, key = { it.id }) { transaction ->
                    TransactionCard(transaction = transaction, onEdit = { editing = transaction })
                }
            }

        }
    }

    editing?.let { transaction ->
        TransactionEditorDialog(
            transaction = transaction,
            onDismiss = { editing = null },
            onSave = viewModel::saveTransaction,
            onDelete = viewModel::deleteTransaction,
        )
    }

}
