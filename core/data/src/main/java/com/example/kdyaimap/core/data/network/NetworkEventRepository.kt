package com.example.kdyaimap.core.data.network

import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.core.model.PoiItem
import com.example.kdyaimap.core.data.network.model.PoiDto
import com.example.kdyaimap.core.data.network.model.PointDto
import com.example.kdyaimap.core.data.network.model.toCampusEvents
import com.example.kdyaimap.core.data.network.model.toPoiDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * 活动网络仓库
 * 处理活动相关的网络请求
 */
@Singleton
class NetworkEventRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    /**
     * 从网络获取POI数据
     */
    suspend fun getPois(category: String? = null): Result<List<PoiDto>> {
        return try {
            val pointDtos = apiService.getPois(category)
            val poiDtos = pointDtos.map { point ->
                PoiDto(
                    id = point.id,
                    name = point.name,
                    latitude = point.latitude,
                    longitude = point.longitude,
                    category = point.category,
                    description = point.description,
                    createdAt = point.createdAt
                )
            }
            Result.success(poiDtos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 从网络获取POI数据并转换为CampusEvent
     */
    suspend fun getPoisAsCampusEvents(category: String? = null): Result<List<CampusEvent>> {
        return try {
            val pointDtos = apiService.getPois(category)
            val poiDtos = pointDtos.map { point ->
                PoiDto(
                    id = point.id,
                    name = point.name,
                    latitude = point.latitude,
                    longitude = point.longitude,
                    category = point.category,
                    description = point.description,
                    createdAt = point.createdAt
                )
            }
            Result.success(poiDtos.toCampusEvents())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 创建新的POI
     */
    suspend fun createPoi(poiDto: PoiDto): Result<PoiDto> {
        return try {
            val pointDto = com.example.kdyaimap.core.data.network.model.PointDto(
                id = poiDto.id,
                name = poiDto.name,
                latitude = poiDto.latitude,
                longitude = poiDto.longitude,
                category = poiDto.category,
                description = poiDto.description,
                createdAt = poiDto.createdAt
            )
            val response = apiService.createPoi(pointDto)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(com.example.kdyaimap.core.data.network.model.PointDto(
                        id = it.id,
                        name = it.name,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        category = it.category,
                        description = it.description,
                        createdAt = it.createdAt
                    ).toPoiDto())
                }
                    ?: Result.failure(Exception("创建POI失败：响应体为空"))
            } else {
                Result.failure(Exception("创建POI失败：${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新POI
     */
    suspend fun updatePoi(id: Long, poiDto: PoiDto): Result<PoiDto> {
        return try {
            val pointDto = com.example.kdyaimap.core.data.network.model.PointDto(
                id = poiDto.id,
                name = poiDto.name,
                latitude = poiDto.latitude,
                longitude = poiDto.longitude,
                category = poiDto.category,
                description = poiDto.description,
                createdAt = poiDto.createdAt
            )
            val response = apiService.updatePoi(id, pointDto)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(com.example.kdyaimap.core.data.network.model.PointDto(
                        id = it.id,
                        name = it.name,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        category = it.category,
                        description = it.description,
                        createdAt = it.createdAt
                    ).toPoiDto())
                }
                    ?: Result.failure(Exception("更新POI失败：响应体为空"))
            } else {
                Result.failure(Exception("更新POI失败：${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除POI
     */
    suspend fun deletePoi(id: Long): Result<Unit> {
        return try {
            val response = apiService.deletePoi(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("删除POI失败：${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有活动
     */
    suspend fun getAllEvents(): Result<List<CampusEvent>> {
        return try {
            val response = apiService.getAllEvents()
            response.handleResponse()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据ID获取活动
     */
    suspend fun getEventById(eventId: String): Result<CampusEvent> {
        return try {
            val response = apiService.getEventById(eventId)
            response.handleResponse()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 创建活动
     */
    suspend fun createEvent(
        title: String,
        description: String,
        eventType: String,
        location: String,
        latitude: Double,
        longitude: Double,
        startTime: Long,
        endTime: Long,
        maxParticipants: Int? = null,
        images: List<String>? = null,
        organizerId: String
    ): Result<CampusEvent> {
        return try {
            val response = apiService.createEvent(
                ApiService.CreateEventRequest(
                    title = title,
                    description = description,
                    eventType = eventType,
                    location = location,
                    latitude = latitude,
                    longitude = longitude,
                    startTime = startTime,
                    endTime = endTime,
                    maxParticipants = maxParticipants,
                    images = images,
                    organizerId = organizerId
                )
            )
            response.handleResponse()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新活动
     */
    suspend fun updateEvent(
        eventId: String,
        title: String? = null,
        description: String? = null,
        eventType: String? = null,
        location: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        startTime: Long? = null,
        endTime: Long? = null,
        maxParticipants: Int? = null,
        images: List<String>? = null,
        status: String? = null
    ): Result<CampusEvent> {
        return try {
            val response = apiService.updateEvent(
                eventId,
                ApiService.UpdateEventRequest(
                    title = title,
                    description = description,
                    eventType = eventType,
                    location = location,
                    latitude = latitude,
                    longitude = longitude,
                    startTime = startTime,
                    endTime = endTime,
                    maxParticipants = maxParticipants,
                    images = images,
                    status = status
                )
            )
            response.handleResponse()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除活动
     */
    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            val response = apiService.deleteEvent(eventId)
            response.handleResponse().map { }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据类型获取活动
     */
    suspend fun getEventsByType(eventType: String): Result<List<CampusEvent>> {
        return try {
            val response = apiService.getEventsByType(eventType)
            response.handleResponse()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取待审核活动
     */
    suspend fun getPendingEvents(): Result<List<CampusEvent>> {
        return try {
            val response = apiService.getPendingEvents()
            response.handleResponse()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取已审核活动
     */
    suspend fun getApprovedEvents(): Result<List<CampusEvent>> {
        return try {
            val response = apiService.getApprovedEvents()
            response.handleResponse()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 上传活动图片
     */
    suspend fun uploadEventImage(eventId: String, imageBytes: ByteArray, fileName: String): Result<String> {
        return try {
            val requestBody = okhttp3.MultipartBody.Builder()
                .setType(okhttp3.MultipartBody.FORM)
                .addFormDataPart("image", fileName,
                    imageBytes.toRequestBody("image/*".toMediaType())
                )
                .build()
            
            val imagePart = requestBody.parts.first()
            val response = apiService.uploadEventImage(eventId, imagePart)
            response.handleResponse()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 搜索活动（通过标题或描述）
     */
    suspend fun searchEvents(query: String): Result<List<CampusEvent>> {
        return try {
            // 先获取所有活动，然后在客户端进行过滤
            // 实际项目中应该在服务端实现搜索接口
            val allEventsResult = getAllEvents()
            allEventsResult.map { events ->
                events.filter { event ->
                    event.title.contains(query, ignoreCase = true) ||
                    event.description.contains(query, ignoreCase = true)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取附近的活动
     */
    suspend fun getNearbyEvents(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 5.0
    ): Result<List<CampusEvent>> {
        return try {
            // 先获取所有活动，然后在客户端进行距离计算
            // 实际项目中应该在服务端实现地理位置查询
            val allEventsResult = getAllEvents()
            allEventsResult.map { events ->
                events.filter { event ->
                    calculateDistance(latitude, longitude, event.latitude, event.longitude) <= radiusKm
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 计算两点之间的距离（公里）
     * 使用Haversine公式
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371 // 地球半径（公里）
        
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        
        val a = Math.sin(latDistance / 2).pow(2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDistance / 2).pow(2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return R * c
    }
}