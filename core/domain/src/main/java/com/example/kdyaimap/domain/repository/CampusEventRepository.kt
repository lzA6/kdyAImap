package com.example.kdyaimap.domain.repository

import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.core.model.EventStatus
import com.example.kdyaimap.core.model.EventType
import kotlinx.coroutines.flow.Flow

interface CampusEventRepository {
    // ==================== 本地数据库方法 ====================
    suspend fun insertEvent(event: CampusEvent)
    suspend fun updateEvent(event: CampusEvent)
    suspend fun deleteEventById(eventId: Long)
    fun getAllApprovedEvents(): Flow<List<CampusEvent>>
    fun getApprovedEventsByType(eventType: EventType): Flow<List<CampusEvent>>
    fun getPendingEvents(): Flow<List<CampusEvent>>
    suspend fun updateEventStatus(eventId: Long, newStatus: EventStatus)
    suspend fun getEventById(eventId: Long): CampusEvent?
    fun getFilteredEvents(
        eventType: EventType?,
        eventStatus: EventStatus?,
        startTime: Long?,
        endTime: Long?
    ): Flow<List<CampusEvent>>
    
    // ==================== 网络请求方法 ====================
    suspend fun getAllEventsNetwork(): Result<List<CampusEvent>>
    suspend fun getEventByIdNetwork(eventId: String): Result<CampusEvent>
    suspend fun createEventNetwork(
        title: String,
        description: String,
        eventType: String,
        location: String,
        latitude: Double,
        longitude: Double,
        startTime: Long,
        endTime: Long,
        maxParticipants: Int?,
        images: List<String>?,
        organizerId: String
    ): Result<CampusEvent>
    suspend fun updateEventNetwork(
        eventId: String,
        title: String?,
        description: String?,
        eventType: String?,
        location: String?,
        latitude: Double?,
        longitude: Double?,
        startTime: Long?,
        endTime: Long?,
        maxParticipants: Int?,
        images: List<String>?,
        status: String?
    ): Result<CampusEvent>
    suspend fun deleteEventNetwork(eventId: String): Result<Unit>
    suspend fun getEventsByTypeNetwork(eventType: String): Result<List<CampusEvent>>
    suspend fun getPendingEventsNetwork(): Result<List<CampusEvent>>
    suspend fun getApprovedEventsNetwork(category: String? = null): Result<List<CampusEvent>>
    suspend fun uploadEventImage(eventId: String, imageBytes: ByteArray, fileName: String): Result<String>
    suspend fun searchEvents(query: String): Result<List<CampusEvent>>
    suspend fun getNearbyEvents(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Result<List<CampusEvent>>
    
    // +++ 新增方法 +++
    /**
     * 从网络获取所有兴趣点(POI)
     */
    suspend fun getPoisFromNetwork(): Result<List<CampusEvent>>
}