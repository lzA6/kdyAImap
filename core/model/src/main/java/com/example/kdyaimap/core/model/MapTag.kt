package com.example.kdyaimap.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey

@Entity(
    tableName = "map_tags",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["creatorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MapTag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: String,
    val icon: String? = null,
    val creatorId: Long,
    val createdAt: Long = System.currentTimeMillis()
)

data class TagWithLocations(
    val tag: MapTag,
    val locations: List<TaggedLocation>
)

data class LocationTag(
    @ColumnInfo(name = "id")
    val locationId: Long,
    @ColumnInfo(name = "tagId")
    val tagId: Long,
    @ColumnInfo(name = "tagName")
    val tagName: String,
    @ColumnInfo(name = "tagColor")
    val tagColor: String,
    @ColumnInfo(name = "name")
    val locationName: String,
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    @ColumnInfo(name = "longitude")
    val longitude: Double
)