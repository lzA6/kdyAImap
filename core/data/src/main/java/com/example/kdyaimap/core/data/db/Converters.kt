package com.example.kdyaimap.core.data.db

import androidx.room.TypeConverter
import com.example.kdyaimap.core.model.EventType
import com.example.kdyaimap.core.model.EventStatus
import com.example.kdyaimap.core.model.HistoryType
import com.example.kdyaimap.core.model.UserRole
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromEventType(value: EventType): String = value.name

    @TypeConverter
    fun toEventType(value: String): EventType = EventType.valueOf(value)

    @TypeConverter
    fun fromEventStatus(value: EventStatus): String = value.name

    @TypeConverter
    fun toEventStatus(value: String): EventStatus = EventStatus.valueOf(value)

    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.valueOf(value)

    @TypeConverter
    fun fromHistoryType(value: HistoryType): String = value.name

    @TypeConverter
    fun toHistoryType(value: String): HistoryType = HistoryType.valueOf(value)

    // List<String> 类型转换器
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return if (value == null) null else Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return if (value == null) null else {
            val listType = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(value, listType)
        }
    }
}