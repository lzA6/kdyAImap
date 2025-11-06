package com.example.kdyaimap.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey

@Entity(
    tableName = "tagged_locations",
    foreignKeys = [
        ForeignKey(
            entity = MapTag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["creatorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TaggedLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tagId: Long,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val description: String? = null,
    val creatorId: Long,
    val createdAt: Long = System.currentTimeMillis()
)