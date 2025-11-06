package com.example.kdyaimap.domain.repository

import com.example.kdyaimap.core.model.*
import kotlinx.coroutines.flow.Flow

interface MapTagRepository {
    // 标签相关
    fun getAllTags(): Flow<List<MapTag>>
    fun getActiveTags(): Flow<List<MapTag>>
    suspend fun getTagById(id: Long): MapTag?
    suspend fun getTagByName(name: String): MapTag?
    suspend fun insertTag(tag: MapTag): Long
    suspend fun updateTag(tag: MapTag)
    suspend fun deleteTag(tag: MapTag)
    suspend fun activateTag(id: Long)
    suspend fun deactivateTag(id: Long)
    suspend fun updateTagSortOrder(id: Long, sortOrder: Int)
    
    // 位置标记相关
    fun getAllLocations(): Flow<List<TaggedLocation>>
    fun getLocationsByTagId(tagId: Long): Flow<List<TaggedLocation>>
    fun getAllLocationsWithTags(): Flow<List<LocationTag>>
    fun getLocationsWithTagsByTagId(tagId: Long): Flow<List<LocationTag>>
    suspend fun getLocationById(id: Long): TaggedLocation?
    suspend fun insertLocation(location: TaggedLocation): Long
    suspend fun updateLocation(location: TaggedLocation)
    suspend fun deleteLocation(location: TaggedLocation)
    suspend fun activateLocation(id: Long)
    suspend fun deactivateLocation(id: Long)
    fun getLocationsInBounds(
        minLat: Double, maxLat: Double, minLng: Double, maxLng: Double
    ): Flow<List<LocationTag>>
    
    // 标签方案相关
    fun getAllSchemes(): Flow<List<TagScheme>>
    fun getActiveSchemes(): Flow<List<TagScheme>>
    suspend fun getSchemeById(id: Long): TagScheme?
    suspend fun getDefaultScheme(): TagScheme?
    suspend fun insertScheme(scheme: TagScheme): Long
    suspend fun updateScheme(scheme: TagScheme)
    suspend fun deleteScheme(scheme: TagScheme)
    suspend fun activateScheme(id: Long)
    suspend fun deactivateScheme(id: Long)
    suspend fun setDefaultScheme(id: Long)
    
    // 复合操作
    suspend fun createTagWithLocations(
        tag: MapTag,
        locations: List<TaggedLocation>
    ): Long
    
    suspend fun getTagWithLocations(tagId: Long): TagWithLocations?
    fun getActiveTagsWithLocationCount(): Flow<List<Pair<MapTag, Int>>>
    
    // 搜索功能
    suspend fun searchTags(query: String): List<MapTag>
    suspend fun searchLocations(query: String): List<LocationTag>
    
    // 数据导入导出
    suspend fun exportTags(): String
    suspend fun importTags(jsonData: String): List<Long>
    suspend fun exportLocations(): String
    suspend fun importLocations(jsonData: String): List<Long>
}