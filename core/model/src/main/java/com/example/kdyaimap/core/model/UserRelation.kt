package com.example.kdyaimap.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "user_relations",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["followerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["followingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["followerId"]),
        Index(value = ["followingId"]),
        Index(value = ["followerId", "followingId"], unique = true)
    ]
)
data class UserRelation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val followerId: Long, // 关注者ID
    val followingId: Long, // 被关注者ID
    val followTimestamp: Long = System.currentTimeMillis()
)

enum class RelationType {
    FOLLOW, // 关注
    BLOCK,  // 拉黑
    MUTUAL  // 互相关注
}