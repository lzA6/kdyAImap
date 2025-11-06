package com.example.kdyaimap.core.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "campus_events",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["authorId"]),
        Index(value = ["eventType"]),
        Index(value = ["status"]),
        Index(value = ["creationTimestamp"]),
        Index(value = ["eventType", "status"]),
        Index(value = ["status", "creationTimestamp"]),
        Index(value = ["eventType", "status", "creationTimestamp"])
    ]
)
data class CampusEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @SerializedName("name") // 后端返回的是 'name'，我们映射到 'title'
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("category") // 后端返回的是 'category'，我们映射到 'eventType'
    val eventType: String, // 使用 String 类型来接收 "FOOD", "STUDY" 等

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    // --- 以下字段后端没有，我们提供默认值或设为可空 ---
    val authorId: Long = 0, // 暂时提供默认值
    val locationName: String = "", // 暂时提供默认值
    val creationTimestamp: Long = System.currentTimeMillis(), // 暂时提供默认值
    val status: EventStatus = EventStatus.APPROVED, // 假设从后端获取的都是已批准的
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val maxParticipants: Int? = null,
    val currentParticipants: Int = 0,
    val images: List<String>? = null,
    val contactInfo: String? = null,
    val requirements: String? = null,
    val tags: List<String>? = null
)