package com.bigcash.core.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

/** Raw notification persistence. Raw rows are never dropped, only reprocessed. */
@Dao
interface RawNotificationDao {

    @Insert
    suspend fun insert(entity: RawNotificationEntity): Long

    @Update
    suspend fun update(entity: RawNotificationEntity)

    @Query("SELECT * FROM raw_notifications ORDER BY postedAt DESC LIMIT :limit OFFSET :offset")
    fun observeRecent(limit: Int, offset: Int): Flow<List<RawNotificationEntity>>

    @Query(
        """
        SELECT * FROM raw_notifications
        WHERE (:packageName IS NULL OR packageName = :packageName)
          AND (:status IS NULL OR parseStatus = :status)
          AND postedAt BETWEEN :from AND :to
        ORDER BY postedAt DESC
        """,
    )
    fun observeFiltered(
        packageName: String?,
        status: String?,
        from: Long,
        to: Long,
    ): Flow<List<RawNotificationEntity>>

    @Query("SELECT * FROM raw_notifications ORDER BY postedAt DESC")
    suspend fun getAll(): List<RawNotificationEntity>

    @Query("SELECT * FROM raw_notifications WHERE id = :id")
    suspend fun findById(id: Long): RawNotificationEntity?

    @Query("SELECT * FROM raw_notifications WHERE contentHash = :hash AND postedAt >= :since ORDER BY postedAt DESC LIMIT 1")
    suspend fun findRecentByHash(hash: String, since: Long): RawNotificationEntity?

    @Query("SELECT COUNT(*) FROM raw_notifications")
    fun observeCount(): Flow<Int>

    @Query("UPDATE raw_notifications SET parseStatus = :status, matchedRuleId = :ruleId WHERE id = :id")
    suspend fun updateParseResult(id: Long, status: String, ruleId: Long?)

    @Query("DELETE FROM raw_notifications")
    suspend fun deleteAll()
}

/** Structured transactions. */
@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(entity: TransactionEntity): Long

    @Insert
    suspend fun insertAll(entities: List<TransactionEntity>)

    @Update
    suspend fun update(entity: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun findById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY occurredAt DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY occurredAt DESC")
    suspend fun getAll(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE rawNotificationId = :rawId")
    suspend fun findByRawNotification(rawId: Long): List<TransactionEntity>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query(
        """
        SELECT * FROM transactions
        WHERE amountMinor = :amount
          AND occurredAt BETWEEN :from AND :to
          AND id != :excludeId
        """,
    )
    suspend fun findPossibleDuplicates(
        amount: Long,
        from: Long,
        to: Long,
        excludeId: Long,
    ): List<TransactionEntity>

    @Query("SELECT DISTINCT tags FROM transactions")
    suspend fun allTagColumns(): List<String>
}
