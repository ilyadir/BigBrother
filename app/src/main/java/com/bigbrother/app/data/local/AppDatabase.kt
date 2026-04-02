package com.bigbrother.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bigbrother.app.data.local.dao.BalanceDao
import com.bigbrother.app.data.local.dao.FocusSessionDao
import com.bigbrother.app.data.local.dao.TrackedAppDao
import com.bigbrother.app.data.local.entity.BalanceEntryEntity
import com.bigbrother.app.data.local.entity.FocusSessionEntity
import com.bigbrother.app.data.local.entity.TrackedAppEntity

@Database(
    entities = [
        BalanceEntryEntity::class,
        TrackedAppEntity::class,
        FocusSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun balanceDao(): BalanceDao
    abstract fun trackedAppDao(): TrackedAppDao
    abstract fun focusSessionDao(): FocusSessionDao
}
