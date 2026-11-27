package com.bigcash.core.model

/** Direction of money movement detected in a notification. */
enum class TransactionType {
    INCOME,
    EXPENSE,
    UNKNOWN,
    ;

    companion object {
        fun fromKey(key: String?): TransactionType =
            entries.firstOrNull { it.name.equals(key?.trim(), ignoreCase = true) } ?: UNKNOWN
    }
}

/** Payment method configured per rule, never hardcoded in the engine. */
enum class PaymentMethod {
    PIX,
    CREDIT_CARD,
    DEBIT_CARD,
    BOLETO,
    TRANSFER,
    CASH,
    OTHER,
    UNKNOWN,
    ;

    companion object {
        fun fromKey(key: String?): PaymentMethod =
            entries.firstOrNull { it.name.equals(key?.trim(), ignoreCase = true) } ?: UNKNOWN
    }
}

/** Outcome of applying the rule cascade to a raw notification. */
enum class ParseStatus {
    SUCCESS,
    PARTIAL,
    FAILED,
    ;

    companion object {
        fun fromKey(key: String?): ParseStatus =
            entries.firstOrNull { it.name.equals(key?.trim(), ignoreCase = true) } ?: FAILED
    }
}

/** How a parsing rule matches the notification text. */
enum class RuleType {
    /** Full regular expression with capture groups. */
    REGEX,

    /** Human friendly mask: `#` is a digit, `*` is any run of characters, everything else is literal. */
    MASK,

    /** Plain keyword list; matches when any keyword is contained in the text. */
    KEYWORD,
    ;

    companion object {
        fun fromKey(key: String?): RuleType =
            entries.firstOrNull { it.name.equals(key?.trim(), ignoreCase = true) } ?: REGEX
    }
}

/** A notification captured verbatim. Raw content is never discarded. */
data class RawNotification(
    val id: Long = 0L,
    val packageName: String,
    val bankKey: String,
    val title: String?,
    val text: String?,
    val bigText: String?,
    val postedAt: Long,
    val contentHash: String,
    val parseStatus: ParseStatus = ParseStatus.FAILED,
    val matchedRuleId: Long? = null,
) {
    /** Everything the bank sent, glued together for parsing. */
    val fullText: String
        get() = listOfNotNull(title, text, bigText)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .joinToString(separator = " ")
}

/** Structured result derived from a [RawNotification]. */
data class Transaction(
    val id: Long = 0L,
    val rawNotificationId: Long?,
    val ruleId: Long?,
    val bankKey: String,
    val packageName: String,
    val type: TransactionType,
    val method: PaymentMethod,
    val amountMinor: Long?,
    val currency: String,
    val cardLastDigits: String?,
    val counterparty: String?,
    val description: String?,
    val occurredAt: Long,
    val parseStatus: ParseStatus,
    val tags: List<String> = emptyList(),
    val isManuallyEdited: Boolean = false,
    val possibleDuplicate: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

/** A monitored package (bank app) with a stable key used by the rule engine. */
data class MonitoredPackage(
    val id: Long = 0L,
    val packageName: String,
    val bankKey: String,
    val displayName: String,
    val isEnabled: Boolean = true,
    val isBuiltIn: Boolean = false,
)
