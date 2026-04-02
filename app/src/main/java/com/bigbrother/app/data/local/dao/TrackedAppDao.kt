package com.bigbrother.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.bigbrother.app.data.local.entity.TrackedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedAppDao {

    @Upsert
    suspend fun upsert(app: TrackedAppEntity)

    @Query("SELECT * FROM tracked_apps WHERE isBlocked = 1 ORDER BY appLabel ASC")
    fun listBlocked(): Flow<List<TrackedAppEntity>>

    @Query("SELECT * FROM tracked_apps ORDER BY appLabel ASC")
    fun listAll(): Flow<List<TrackedAppEntity>>
}
