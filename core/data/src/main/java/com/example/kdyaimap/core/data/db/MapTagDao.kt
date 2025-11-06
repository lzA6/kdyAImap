package com.example.kdyaimap.core.data.db

import androidx.room.*
import com.example.kdyaimap.core.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MapTagDao {
    
    @Query("SELECT * FROM map_tags ORDER BY name ASC")
    fun getAllActiveTags(): Flow<List<MapTag>>
    
    @Query("SELECT * FROM map_tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<MapTag>>
    
    @Query("SELECT * FROM map_tags WHERE id = :id")
    suspend fun getTagById(id: Long): MapTag?
    
    @Query("SELECT * FROM map_tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): MapTag?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: MapTag): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<MapTag>): List<Long>
    
    @Update
    suspend fun updateTag(tag: MapTag)
    
    @Delete
    suspend fun deleteTag(tag: MapTag)
    
    @Query("DELETE FROM map_tags WHERE id = :id")
    suspend fun deactivateTag(id: Long)
    
    @Query("DELETE FROM map_tags WHERE id = :id")
    suspend fun activateTag(id: Long)
    
    @Query("UPDATE map_tags SET name = name WHERE id = :id")
    suspend fun updateTagSortOrder(id: Long)
    
    @Query("DELETE FROM map_tags WHERE id = :id")
    suspend fun deleteTagById(id: Long)
}

@Dao
interface TaggedLocationDao {
    
    @Query("SELECT * FROM tagged_locations ORDER BY createdAt DESC")
    fun getAllActiveLocations(): Flow<List<TaggedLocation>>
    
    @Query("SELECT * FROM tagged_locations WHERE tagId = :tagId ORDER BY createdAt DESC")
    fun getLocationsByTagId(tagId: Long): Flow<List<TaggedLocation>>
    
    @Query("SELECT * FROM tagged_locations WHERE id = :id")
    suspend fun getLocationById(id: Long): TaggedLocation?
    
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("""
        SELECT tl.id as id, tl.tagId, tl.latitude, tl.longitude,
               mt.name as tagName, mt.color as tagColor, tl.title as name
        FROM tagged_locations tl
        INNER JOIN map_tags mt ON tl.tagId = mt.id
        ORDER BY tl.createdAt DESC
    """)
    fun getAllLocationsWithTags(): Flow<List<LocationTag>>
    
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("""
        SELECT tl.id as id, tl.tagId, tl.latitude, tl.longitude,
               mt.name as tagName, mt.color as tagColor, tl.title as name
        FROM tagged_locations tl
        INNER JOIN map_tags mt ON tl.tagId = mt.id
        WHERE tl.tagId = :tagId
        ORDER BY tl.createdAt DESC
    """)
    fun getLocationsWithTagsByTagId(tagId: Long): Flow<List<LocationTag>>
    
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("""
        SELECT tl.id as id, tl.tagId, tl.latitude, tl.longitude,
               mt.name as tagName, mt.color as tagColor, tl.title as name
        FROM tagged_locations tl
        INNER JOIN map_tags mt ON tl.tagId = mt.id
        WHERE tl.latitude BETWEEN :minLat AND :maxLat
        AND tl.longitude BETWEEN :minLng AND :maxLng
        ORDER BY tl.createdAt DESC
    """)
    fun getLocationsInBounds(
        minLat: Double, maxLat: Double, minLng: Double, maxLng: Double
    ): Flow<List<LocationTag>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: TaggedLocation): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<TaggedLocation>): List<Long>
    
    @Update
    suspend fun updateLocation(location: TaggedLocation)
    
    @Delete
    suspend fun deleteLocation(location: TaggedLocation)
    
    @Query("DELETE FROM tagged_locations WHERE id = :id")
    suspend fun deactivateLocation(id: Long)
    
    @Query("DELETE FROM tagged_locations WHERE id = :id")
    suspend fun activateLocation(id: Long)
    
    @Query("DELETE FROM tagged_locations WHERE id = :id")
    suspend fun deleteLocationById(id: Long)
    
    @Query("DELETE FROM tagged_locations WHERE tagId = :tagId")
    suspend fun deleteLocationsByTagId(tagId: Long)
}

@Dao
interface TagSchemeDao {
    
    @Query("SELECT * FROM tag_schemes ORDER BY name ASC")
    fun getAllActiveSchemes(): Flow<List<TagScheme>>
    
    @Query("SELECT * FROM tag_schemes ORDER BY name ASC")
    fun getAllSchemes(): Flow<List<TagScheme>>
    
    @Query("SELECT * FROM tag_schemes WHERE id = :id")
    suspend fun getSchemeById(id: Long): TagScheme?
    
    @Query("SELECT * FROM tag_schemes LIMIT 1")
    suspend fun getDefaultScheme(): TagScheme?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheme(scheme: TagScheme): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchemes(schemes: List<TagScheme>): List<Long>
    
    @Update
    suspend fun updateScheme(scheme: TagScheme)
    
    @Delete
    suspend fun deleteScheme(scheme: TagScheme)
    
    @Query("DELETE FROM tag_schemes WHERE id = :id")
    suspend fun deactivateScheme(id: Long)
    
    @Query("DELETE FROM tag_schemes WHERE id = :id")
    suspend fun activateScheme(id: Long)
    
    @Query("DELETE FROM tag_schemes")
    suspend fun clearDefaultScheme()
    
    @Query("DELETE FROM tag_schemes WHERE id = :id")
    suspend fun setDefaultScheme(id: Long)
    
    @Query("DELETE FROM tag_schemes WHERE id = :id")
    suspend fun deleteSchemeById(id: Long)
}