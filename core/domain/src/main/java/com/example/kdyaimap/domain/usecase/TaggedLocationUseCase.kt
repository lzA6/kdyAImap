package com.example.kdyaimap.domain.usecase

import com.example.kdyaimap.core.model.LocationTag
import com.example.kdyaimap.core.model.TaggedLocation
import com.example.kdyaimap.domain.repository.MapTagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllLocationsUseCase @Inject constructor(
    private val mapTagRepository: MapTagRepository
) {
    operator fun invoke(): Flow<List<TaggedLocation>> = mapTagRepository.getAllLocations()
}

class GetLocationsWithTagsUseCase @Inject constructor(
    private val mapTagRepository: MapTagRepository
) {
    operator fun invoke(): Flow<List<LocationTag>> = mapTagRepository.getAllLocationsWithTags()
}

class GetLocationsByTagIdUseCase @Inject constructor(
    private val mapTagRepository: MapTagRepository
) {
    operator fun invoke(tagId: Long): Flow<List<TaggedLocation>> = 
        mapTagRepository.getLocationsByTagId(tagId)
}

class GetLocationsWithTagsByTagIdUseCase @Inject constructor(
    private val mapTagRepository: MapTagRepository
) {
    operator fun invoke(tagId: Long): Flow<List<LocationTag>> = 
        mapTagRepository.getLocationsWithTagsByTagId(tagId)
}

class CreateLocationUseCase @Inject constructor(
    private val mapTagRepository: MapTagRepository
) {
    suspend operator fun invoke(location: TaggedLocation): Long {
        // 验证位置标题不能为空
        require(location.title.isNotBlank()) { "位置标题不能为空" }
        
        // 验证坐标有效性
        require(location.latitude in -90.0..90.0) { "纬度必须在-90到90之间" }
        require(location.longitude in -180.0..180.0) { "经度必须在-180到180之间" }
        
        // 验证标签是否存在
        val tag = mapTagRepository.getTagById(location.tagId)
        require(tag != null) { "指定的标签不存在" }
        
        return mapTagRepository.insertLocation(
            location.copy(title = location.title.trim())
        )
    }
}

class UpdateLocationUseCase @Inject constructor(
    private val mapTagRepository: MapTagRepository
) {
    suspend operator fun invoke(location: TaggedLocation) {
        require(location.title.isNotBlank()) { "位置标题不能为空" }
        require(location.latitude in -90.0..90.0) { "纬度必须在-90到90之间" }
        require(location.longitude in -180.0..180.0) { "经度必须在-180到180之间" }
        
        // 验证标签是否存在
        val tag = mapTagRepository.getTagById(location.tagId)
        require(tag != null) { "指定的标签不存在" }
        
        mapTagRepository.updateLocation(
            location.copy(title = location.title.trim())
        )
    }
}

class DeleteLocationUseCase @Inject constructor(
    private val mapTagRepository: MapTagRepository
) {
    suspend operator fun invoke(location: TaggedLocation) {
        mapTagRepository.deleteLocation(location)
    }
}

class GetLocationsInBoundsUseCase @Inject constructor(
    private val mapTagRepository: MapTagRepository
) {
    operator fun invoke(
        minLat: Double, maxLat: Double, minLng: Double, maxLng: Double
    ): Flow<List<LocationTag>> {
        require(minLat < maxLat) { "最小纬度必须小于最大纬度" }
        require(minLng < maxLng) { "最小经度必须小于最大经度" }
        require(minLat >= -90.0 && maxLat <= 90.0) { "纬度范围必须在-90到90之间" }
        require(minLng >= -180.0 && maxLng <= 180.0) { "经度范围必须在-180到180之间" }
        
        return mapTagRepository.getLocationsInBounds(minLat, maxLat, minLng, maxLng)
    }
}

class SearchLocationsUseCase @Inject constructor(
    private val mapTagRepository: MapTagRepository
) {
    suspend operator fun invoke(query: String): List<LocationTag> {
        require(query.isNotBlank()) { "搜索关键词不能为空" }
        return mapTagRepository.searchLocations(query.trim())
    }
}