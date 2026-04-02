package com.bigbrother.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bigbrother.app.data.local.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Insert
    suspend fun startSession(session: FocusSessionEntity): Long

    @Update
    suspend fun finishSession(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions WHERE startedAt >= (strftime('%s', 'now', 'start of day') * 1000) ORDER BY startedAt DESC")
    fun listToday(): Flow<List<FocusSessionEntity>>
}
