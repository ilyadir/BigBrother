package com.bigbrother.app.data.local

import androidx.room.TypeConverter
import com.bigbrother.app.domain.model.EntryType

class RoomTypeConverters {

    @TypeConverter
    fun fromEntryType(value: EntryType): String = value.name

    @TypeConverter
    fun toEntryType(value: String): EntryType = EntryType.valueOf(value)
}
