package com.bigcash.app.ui.dashboard

import com.bigcash.core.model.ParseStatus
import com.bigcash.core.model.PaymentMethod
import com.bigcash.core.model.Transaction
import com.bigcash.core.model.TransactionType

/** Time window selector for the dashboard. */
enum class PeriodFilter { ALL, TODAY, WEEK, MONTH, CUSTOM }

/** Every dashboard filter that can be combined. */
data class TransactionFilters(
    val query: String = "",
    val bankKeys: Set<String> = emptySet(),
    val type: TransactionType? = null,
    val method: PaymentMethod? = null,
    val period: PeriodFilter = PeriodFilter.MONTH,
    val customFrom: Long? = null,
    val customTo: Long? = null,
    val statuses: Set<ParseStatus> = emptySet(),
    val tags: Set<String> = emptySet(),
    val minMinor: Long? = null,
    val maxMinor: Long? = null,
    val duplicatesOnly: Boolean = false,
)

data class DashboardUiState(
    val transactions: List<Transaction> = emptyList(),
    val filters: TransactionFilters = TransactionFilters(),
    val totalIncomeMinor: Long = 0L,
    val totalExpenseMinor: Long = 0L,
    val currency: String = "BRL",
    val availableTags: List<String> = emptyList(),
    val availableBanks: List<String> = emptyList(),
)
