package com.bigcash.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigcash.app.data.NotificationIngestRepository
import com.bigcash.app.data.SettingsRepository
import com.bigcash.core.model.ParseStatus
import com.bigcash.core.model.PaymentMethod
import com.bigcash.core.model.Transaction
import com.bigcash.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val ingestRepository: NotificationIngestRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val filters = MutableStateFlow(TransactionFilters())

    val state: StateFlow<DashboardUiState> = ingestRepository.observeTransactions()
        .combine(filters) { transactions, currentFilters -> transactions to currentFilters }
        .combine(settingsRepository.settings) { (transactions, currentFilters), settings ->
            val filtered = applyFilters(transactions, currentFilters)
            DashboardUiState(
                transactions = filtered,
                filters = currentFilters,
                totalIncomeMinor = filtered.filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amountMinor ?: 0L },
                totalExpenseMinor = filtered.filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amountMinor ?: 0L },
                currency = settings.parsing.defaultCurrency,
                availableTags = transactions.flatMap { it.tags }.distinct().sorted(),
                availableBanks = transactions.map { it.bankKey }.distinct().sorted(),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun onQueryChange(value: String) = update { it.copy(query = value) }

    fun toggleBank(bankKey: String) = update { current ->
        current.copy(
            bankKeys = if (current.bankKeys.contains(bankKey)) current.bankKeys - bankKey else current.bankKeys + bankKey,
        )
    }

    fun setType(type: TransactionType?) = update { it.copy(type = type) }

    fun setMethod(method: PaymentMethod?) = update { it.copy(method = method) }

    fun setPeriod(period: PeriodFilter) = update { it.copy(period = period) }

    fun setAmountRange(minMinor: Long?, maxMinor: Long?) = update {
        it.copy(minMinor = minMinor, maxMinor = maxMinor)
    }

    fun toggleStatus(status: ParseStatus) = update { current ->
        current.copy(
            statuses = if (current.statuses.contains(status)) current.statuses - status else current.statuses + status,
        )
    }

    fun toggleTag(tag: String) = update { current ->
        current.copy(tags = if (current.tags.contains(tag)) current.tags - tag else current.tags + tag)
    }

    fun toggleDuplicatesOnly() = update { it.copy(duplicatesOnly = !it.duplicatesOnly) }

    fun clearFilters() = update { TransactionFilters(period = PeriodFilter.MONTH) }

    fun saveTransaction(transaction: Transaction) {
        viewModelScope.launch { ingestRepository.updateTransaction(transaction) }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch { ingestRepository.deleteTransaction(id) }
    }

    private fun update(transform: (TransactionFilters) -> TransactionFilters) {
        filters.value = transform(filters.value)
    }

    private fun applyFilters(items: List<Transaction>, filters: TransactionFilters): List<Transaction> {
        val range = resolveRange(filters)
        return items.asSequence()
            .filter { filters.bankKeys.isEmpty() || filters.bankKeys.contains(it.bankKey) }
            .filter { filters.type == null || it.type == filters.type }
            .filter { filters.method == null || it.method == filters.method }
            .filter { filters.statuses.isEmpty() || filters.statuses.contains(it.parseStatus) }
            .filter { filters.tags.isEmpty() || it.tags.any(filters.tags::contains) }
            .filter { filters.minMinor == null || (it.amountMinor ?: 0L) >= filters.minMinor!! }
            .filter { filters.maxMinor == null || (it.amountMinor ?: 0L) <= filters.maxMinor!! }
            .filter { !filters.duplicatesOnly || it.possibleDuplicate }
            .filter { it.occurredAt in range.first..range.second }
            .filter { matchesQuery(it, filters.query) }
            .sortedByDescending { it.occurredAt }
            .toList()
    }

    private fun matchesQuery(transaction: Transaction, query: String): Boolean {
        if (query.isBlank()) return true
        val needle = query.trim().lowercase()
        return listOfNotNull(
            transaction.description,
            transaction.counterparty,
            transaction.bankKey,
            transaction.cardLastDigits,
            transaction.tags.joinToString(" "),
        ).any { it.lowercase().contains(needle) }
    }

    private fun resolveRange(filters: TransactionFilters): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val now = Instant.now().toEpochMilli()
        val today = LocalDate.now(zone)
        val from = when (filters.period) {
            PeriodFilter.ALL -> 0L
            PeriodFilter.TODAY -> today.atStartOfDay(zone).toInstant().toEpochMilli()
            PeriodFilter.WEEK -> today.minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli()
            PeriodFilter.MONTH -> today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
            PeriodFilter.CUSTOM -> filters.customFrom ?: 0L
        }
        val to = if (filters.period == PeriodFilter.CUSTOM) filters.customTo ?: now else now
        return from to to
    }
}
