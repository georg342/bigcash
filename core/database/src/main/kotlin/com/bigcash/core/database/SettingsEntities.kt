package com.bigcash.core.database

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/** Fully configurable parsing rule row. */
@Entity(
    tableName = "parsing_rules",
    indices = [Index("bankKey"), Index("priority")],
)
data class ParsingRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val bankKey: String,
    val type: String,
    val pattern: String,
    val priority: Int,
    val isActive: Boolean,
    val description: String,
    val amountGroup: Int?,
    val counterpartyGroup: Int?,
    val cardGroup: Int?,
    val typeValue: String?,
    val methodValue: String?,
    val currencyValue: String?,
)

/** Monitored bank app package. */
@Entity(
    tableName = "monitored_packages",
    indices = [Index(value = ["packageName"], unique = true)],
)
data class MonitoredPackageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val packageName: String,
    val bankKey: String,
    val displayName: String,
    val isEnabled: Boolean,
    val isBuiltIn: Boolean,
)

/** Key/value store for every configurable parameter of the app. */
@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String,
)
