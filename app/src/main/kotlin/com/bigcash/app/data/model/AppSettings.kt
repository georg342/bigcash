package com.bigcash.app.data.model

import com.bigcash.core.model.AppNotificationConfig
import com.bigcash.core.model.ParsingConfig
import com.bigcash.core.model.ThemeMode

/** Aggregated view of every configurable parameter, exposed to the whole app. */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val successTemplate: String = "",
    val failureTemplate: String = "",
    val parsing: ParsingConfig = ParsingConfig(),
    val appNotification: AppNotificationConfig = AppNotificationConfig(),
)
