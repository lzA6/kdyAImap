package com.example.kdyaimap.core.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_history",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CampusEvent::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["eventId"])]
)
data class UserHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val eventId: Long,
    val historyType: HistoryType,
    val timestamp: Long = System.currentTimeMillis()
)