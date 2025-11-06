package com.example.kdyaimap.core.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TagSchemeConverters {
    
    @TypeConverter
    fun fromStringList(value: List<Long>): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<Long> {
        val listType = object : TypeToken<List<Long>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }
}