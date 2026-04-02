package com.bigbrother.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bigbrother.app.data.local.entity.BalanceEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BalanceDao {

    @Insert
    suspend fun insert(entry: BalanceEntryEntity)

    @Query("SELECT COALESCE(SUM(minutes), 0) FROM balance_entries WHERE type = 'EARN'")
    fun sumEarn(): Flow<Int>

    @Query("SELECT COALESCE(SUM(minutes), 0) FROM balance_entries WHERE type = 'SPEND'")
    fun sumSpend(): Flow<Int>

    @Query(
        "SELECT " +
            "COALESCE(SUM(CASE WHEN type = 'EARN' THEN minutes ELSE 0 END), 0) - " +
            "COALESCE(SUM(CASE WHEN type = 'SPEND' THEN minutes ELSE 0 END), 0) " +
            "FROM balance_entries"
    )
    fun streamBalance(): Flow<Int>

    @Query(
        "SELECT COALESCE(SUM(minutes), 0) " +
            "FROM balance_entries " +
            "WHERE type = 'EARN' AND createdAt BETWEEN :dayStartMillis AND :dayEndMillis"
    )
    fun sumEarnForPeriod(dayStartMillis: Long, dayEndMillis: Long): Flow<Int>

    @Query(
        "SELECT COALESCE(SUM(minutes), 0) " +
            "FROM balance_entries " +
            "WHERE type = 'SPEND' AND createdAt BETWEEN :dayStartMillis AND :dayEndMillis"
    )
    fun sumSpendForPeriod(dayStartMillis: Long, dayEndMillis: Long): Flow<Int>

    @Query("SELECT * FROM balance_entries ORDER BY createdAt DESC LIMIT :limit")
    fun latestEntries(limit: Int): Flow<List<BalanceEntryEntity>>
}
