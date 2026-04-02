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
}
