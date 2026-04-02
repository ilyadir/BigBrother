package com.bigbrother.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bigbrother.app.domain.model.EntryType

@Entity(tableName = "balance_entries")
data class BalanceEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val minutes: Int,
    val type: EntryType,
    val note: String,
    val category: String?
)
