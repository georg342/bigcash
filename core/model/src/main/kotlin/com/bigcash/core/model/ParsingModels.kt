package com.bigcash.core.model

/**
 * One fully configurable parsing rule.
 *
 * Every value the engine consumes lives here and is editable from the calibration panel,
 * so no regex, keyword, mask or currency symbol is compiled into the code.
 */
data class ParsingRule(
    val id: Long = 0L,
    val name: String,
    val bankKey: String,
    val type: RuleType,
    val pattern: String,
    val priority: Int,
    val isActive: Boolean = true,
    val description: String = "",
    val amountGroup: Int? = null,
    val counterpartyGroup: Int? = null,
    val cardGroup: Int? = null,
    val typeValue: String? = null,
    val methodValue: String? = null,
    val currencyValue: String? = null,
)

/** Short trace entry describing what a single rule did during a parse (playground / audit). */
data class RuleTrace(
    val ruleId: Long,
    val ruleName: String,
    val ruleType: RuleType,
    val priority: Int,
    val matched: Boolean,
    val matchedText: String? = null,
    val amountMinor: Long? = null,
    val type: TransactionType = TransactionType.UNKNOWN,
    val method: PaymentMethod = PaymentMethod.UNKNOWN,
    val note: String = "",
)

/** Result of running the whole cascade. */
data class ParseOutcome(
    val status: ParseStatus,
    val bankKey: String,
    val amountMinor: Long? = null,
    val currency: String = "",
    val type: TransactionType = TransactionType.UNKNOWN,
    val method: PaymentMethod = PaymentMethod.UNKNOWN,
    val cardLastDigits: String? = null,
    val counterparty: String? = null,
    val description: String? = null,
    val matchedRuleId: Long? = null,
    val traces: List<RuleTrace> = emptyList(),
)

/** Everything the parsing engine may use, persisted in the settings table and editable in the UI. */
data class ParsingConfig(
    val currencySymbols: List<String> = emptyList(),
    val decimalSeparator: Char = ',',
    val thousandsSeparator: Char = '.',
    val incomeKeywords: List<String> = emptyList(),
    val expenseKeywords: List<String> = emptyList(),
    val methodKeywords: Map<String, List<String>> = emptyMap(),
    val fallbackAmountPattern: String = "",
    val defaultCurrency: String = "BRL",
    val dedupeWindowMillis: Long = 60_000L,
    val minAmountMinor: Long = 1L,
    val maxAmountMinor: Long = 100_000_000L,
)

/** Settings that drive the app's own notifications. */
data class AppNotificationConfig(
    val enabled: Boolean = true,
    val successTemplate: String = "",
    val failureTemplate: String = "",
    val channelId: String = "bigcash_transactions",
    val channelName: String = "Recorded transactions",
    val channelDescription: String = "",
)

/** Theme choice exposed in the settings screen. */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
    ;

    companion object {
        fun fromKey(key: String?): ThemeMode =
            entries.firstOrNull { it.name.equals(key?.trim(), ignoreCase = true) } ?: SYSTEM
    }
}
