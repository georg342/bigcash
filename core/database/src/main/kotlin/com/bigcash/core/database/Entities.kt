package com.bigcash.core.database

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

/** Every notification captured, stored verbatim. Raw content is never discarded. */
@Entity(
    tableName = "raw_notifications",
    indices = [
        Index("contentHash"),
        Index("postedAt"),
        Index("parseStatus"),
        Index("packageName"),
    ],
)
data class RawNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "packageName") val packageName: String,
    @ColumnInfo(name = "bankKey") val bankKey: String,
    val title: String?,
    val text: String?,
    val bigText: String?,
    val postedAt: Long,
    val contentHash: String,
    val parseStatus: String,
    val matchedRuleId: Long?,
)

/** Structured transaction derived from a raw notification (or created manually). */
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["rawNotificationId"]),
        Index(value = ["occurredAt"]),
        Index(value = ["bankKey"]),
        Index(value = ["parseStatus"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = RawNotificationEntity::class,
            parentColumns = ["id"],
            childColumns = ["rawNotificationId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val rawNotificationId: Long?,
    val ruleId: Long?,
    val bankKey: String,
    val packageName: String,
    val type: String,
    val method: String,
    val amountMinor: Long?,
    val currency: String,
    val cardLastDigits: String?,
    val counterparty: String?,
    val description: String?,
    val occurredAt: Long,
    val parseStatus: String,
    val tags: List<String>,
    val isManuallyEdited: Boolean,
    val possibleDuplicate: Boolean,
    val createdAt: Long,
)
