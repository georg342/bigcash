package com.bigcash.app.data

import com.bigcash.app.data.model.AppSettings
import com.bigcash.core.database.AppSettingEntity
import com.bigcash.core.model.ParsingConfig
import com.bigcash.core.model.ThemeMode

/** Maps [AppSettings] to and from `app_settings` rows. */
object SettingsCodec {

    fun toRows(settings: AppSettings): List<AppSettingEntity> {
        val parsing = settings.parsing
        val notification = settings.appNotification
        return listOf(
            ValueCodec.row(SettingsKeys.THEME_MODE, settings.themeMode.name),
            ValueCodec.row(SettingsKeys.NOTIFICATIONS_ENABLED, settings.notificationsEnabled.toString()),
            ValueCodec.row(SettingsKeys.ONBOARDING_COMPLETED, settings.onboardingCompleted.toString()),
            ValueCodec.row(SettingsKeys.SUCCESS_TEMPLATE, settings.successTemplate),
            ValueCodec.row(SettingsKeys.FAILURE_TEMPLATE, settings.failureTemplate),
            ValueCodec.row(SettingsKeys.CURRENCY_SYMBOLS, ValueCodec.encodeList(parsing.currencySymbols)),
            ValueCodec.row(SettingsKeys.DECIMAL_SEPARATOR, parsing.decimalSeparator.toString()),
            ValueCodec.row(SettingsKeys.THOUSANDS_SEPARATOR, parsing.thousandsSeparator.toString()),
            ValueCodec.row(SettingsKeys.INCOME_KEYWORDS, ValueCodec.encodeList(parsing.incomeKeywords)),
            ValueCodec.row(SettingsKeys.EXPENSE_KEYWORDS, ValueCodec.encodeList(parsing.expenseKeywords)),
            ValueCodec.row(SettingsKeys.METHOD_KEYWORDS, ValueCodec.encodeMap(parsing.methodKeywords)),
            ValueCodec.row(SettingsKeys.FALLBACK_AMOUNT_PATTERN, parsing.fallbackAmountPattern),
            ValueCodec.row(SettingsKeys.DEFAULT_CURRENCY, parsing.defaultCurrency),
            ValueCodec.row(SettingsKeys.DEDUPE_WINDOW, parsing.dedupeWindowMillis.toString()),
            ValueCodec.row(SettingsKeys.MIN_AMOUNT, parsing.minAmountMinor.toString()),
            ValueCodec.row(SettingsKeys.MAX_AMOUNT, parsing.maxAmountMinor.toString()),
            ValueCodec.row(SettingsKeys.CHANNEL_ID, notification.channelId),
            ValueCodec.row(SettingsKeys.CHANNEL_NAME, notification.channelName),
            ValueCodec.row(SettingsKeys.CHANNEL_DESCRIPTION, notification.channelDescription),
        )
    }

    fun fromRows(defaults: AppSettings, rows: List<AppSettingEntity>): AppSettings {
        val map = rows.associate { it.key to it.value }
        val parsing = defaults.parsing
        val notification = defaults.appNotification
        val enabled = map[SettingsKeys.NOTIFICATIONS_ENABLED]?.toBooleanStrictOrNull()
            ?: defaults.notificationsEnabled

        return AppSettings(
            themeMode = map[SettingsKeys.THEME_MODE]?.let(ThemeMode::fromKey) ?: defaults.themeMode,
            notificationsEnabled = enabled,
            onboardingCompleted = map[SettingsKeys.ONBOARDING_COMPLETED]?.toBooleanStrictOrNull()
                ?: defaults.onboardingCompleted,
            successTemplate = map[SettingsKeys.SUCCESS_TEMPLATE] ?: defaults.successTemplate,
            failureTemplate = map[SettingsKeys.FAILURE_TEMPLATE] ?: defaults.failureTemplate,
            parsing = ParsingConfig(
                currencySymbols = map[SettingsKeys.CURRENCY_SYMBOLS]?.let(ValueCodec::decodeList)
                    ?: parsing.currencySymbols,
                decimalSeparator = map[SettingsKeys.DECIMAL_SEPARATOR]?.firstOrNull()
                    ?: parsing.decimalSeparator,
                thousandsSeparator = map[SettingsKeys.THOUSANDS_SEPARATOR]?.firstOrNull()
                    ?: parsing.thousandsSeparator,
                incomeKeywords = map[SettingsKeys.INCOME_KEYWORDS]?.let(ValueCodec::decodeList)
                    ?: parsing.incomeKeywords,
                expenseKeywords = map[SettingsKeys.EXPENSE_KEYWORDS]?.let(ValueCodec::decodeList)
                    ?: parsing.expenseKeywords,
                methodKeywords = map[SettingsKeys.METHOD_KEYWORDS]?.let(ValueCodec::decodeMap)
                    ?: parsing.methodKeywords,
                fallbackAmountPattern = map[SettingsKeys.FALLBACK_AMOUNT_PATTERN]
                    ?: parsing.fallbackAmountPattern,
                defaultCurrency = map[SettingsKeys.DEFAULT_CURRENCY] ?: parsing.defaultCurrency,
                dedupeWindowMillis = map[SettingsKeys.DEDUPE_WINDOW]?.toLongOrNull()
                    ?: parsing.dedupeWindowMillis,
                minAmountMinor = map[SettingsKeys.MIN_AMOUNT]?.toLongOrNull() ?: parsing.minAmountMinor,
                maxAmountMinor = map[SettingsKeys.MAX_AMOUNT]?.toLongOrNull() ?: parsing.maxAmountMinor,
            ),
            appNotification = notification.copy(
                enabled = enabled,
                successTemplate = map[SettingsKeys.SUCCESS_TEMPLATE] ?: notification.successTemplate,
                failureTemplate = map[SettingsKeys.FAILURE_TEMPLATE] ?: notification.failureTemplate,
                channelId = map[SettingsKeys.CHANNEL_ID] ?: notification.channelId,
                channelName = map[SettingsKeys.CHANNEL_NAME] ?: notification.channelName,
                channelDescription = map[SettingsKeys.CHANNEL_DESCRIPTION]
                    ?: notification.channelDescription,
            ),
        )
    }
}
