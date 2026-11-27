package com.bigcash.app.data

import com.bigcash.core.database.AppSettingEntity

/** Keys used in the `app_settings` table. Every configurable parameter has one. */
object SettingsKeys {
    const val THEME_MODE = "theme_mode"
    const val NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val ONBOARDING_COMPLETED = "onboarding_completed"
    const val SUCCESS_TEMPLATE = "notification_success_template"
    const val FAILURE_TEMPLATE = "notification_failure_template"
    const val CURRENCY_SYMBOLS = "parsing_currency_symbols"
    const val DECIMAL_SEPARATOR = "parsing_decimal_separator"
    const val THOUSANDS_SEPARATOR = "parsing_thousands_separator"
    const val INCOME_KEYWORDS = "parsing_income_keywords"
    const val EXPENSE_KEYWORDS = "parsing_expense_keywords"
    const val METHOD_KEYWORDS = "parsing_method_keywords"
    const val FALLBACK_AMOUNT_PATTERN = "parsing_fallback_amount_pattern"
    const val DEFAULT_CURRENCY = "parsing_default_currency"
    const val DEDUPE_WINDOW = "parsing_dedupe_window_millis"
    const val MIN_AMOUNT = "parsing_min_amount_minor"
    const val MAX_AMOUNT = "parsing_max_amount_minor"
    const val CHANNEL_ID = "notification_channel_id"
    const val CHANNEL_NAME = "notification_channel_name"
    const val CHANNEL_DESCRIPTION = "notification_channel_description"
}

/**
 * Encodes settings rows so they fit a plain key/value table.
 * Lists use `|` as separator, method keyword maps use `KEY=kw1,kw2;KEY2=...`.
 */
object ValueCodec {

    private const val LIST_SEPARATOR = "|"
    private const val ENTRY_SEPARATOR = ";"
    private const val KEYWORD_SEPARATOR = ","

    fun encodeList(values: List<String>): String = values
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(LIST_SEPARATOR)

    fun decodeList(value: String): List<String> = value
        .split(LIST_SEPARATOR)
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    fun encodeMap(values: Map<String, List<String>>): String = values.entries.joinToString(ENTRY_SEPARATOR) { entry ->
        "${entry.key}=${entry.value.joinToString(KEYWORD_SEPARATOR)}"
    }

    fun decodeMap(value: String): Map<String, List<String>> = value
        .split(ENTRY_SEPARATOR)
        .mapNotNull { entry ->
            val parts = entry.split('=', limit = 2)
            if (parts.size != 2) return@mapNotNull null
            val keywords = parts[1].split(KEYWORD_SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }
            parts[0].trim() to keywords
        }
        .toMap()

    fun row(key: String, value: String): AppSettingEntity = AppSettingEntity(key = key, value = value)
}
