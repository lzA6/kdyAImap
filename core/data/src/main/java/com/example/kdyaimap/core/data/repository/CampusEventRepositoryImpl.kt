package com.example.kdyaimap.core.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.kdyaimap.core.data.db.CampusEventDao
import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.core.model.EventStatus
import com.example.kdyaimap.core.model.EventType
import com.example.kdyaimap.core.data.di.IoDispatcher
import com.example.kdyaimap.domain.repository.CampusEventRepository
import com.example.kdyaimap.core.data.network.ApiService
import com.example.kdyaimap.core.data.network.handleResponse
import com.example.kdyaimap.core.data.network.model.toCampusEvents
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CampusEventRepositoryImpl @Inject constructor(
    private val campusEventDao: CampusEventDao,
    private val apiService: ApiService,
    private val networkEventRepository: com.example.kdyaimap.core.data.network.NetworkEventRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : CampusEventRepository {

    // ==================== 本地数据库方法 ====================

    override suspend fun insertEvent(event: CampusEvent) {
        campusEventDao.insertEvent(event)
    }

    override suspend fun updateEvent(event: CampusEvent) {
        campusEventDao.updateEvent(event)
    }

    override suspend fun deleteEventById(eventId: Long) {
        campusEventDao.deleteEventById(eventId)
    }

    override fun getAllApprovedEvents(): Flow<List<CampusEvent>> {
        return campusEventDao.getAllApprovedEvents().flowOn(dispatcher)
    }

    override fun getApprovedEventsByType(eventType: EventType): Flow<List<CampusEvent>> {
        return campusEventDao.getApprovedEventsByType(eventType.name).flowOn(dispatcher)
    }

    override fun getPendingEvents(): Flow<List<CampusEvent>> {
        return campusEventDao.getPendingEvents().flowOn(dispatcher)
    }

    override suspend fun updateEventStatus(eventId: Long, newStatus: EventStatus) {
        campusEventDao.updateEventStatus(eventId, newStatus.name)
    }

    override suspend fun getEventById(eventId: Long): CampusEvent? {
        return campusEventDao.getEventById(eventId)
    }

    override fun getFilteredEvents(
        eventType: EventType?,
        eventStatus: EventStatus?,
        startTime: Long?,
        endTime: Long?
    ): Flow<List<CampusEvent>> {
        val queryBuilder = StringBuilder("SELECT * FROM campus_events WHERE 1=1")
        val args = mutableListOf<Any>()

        eventType?.let {
            queryBuilder.append(" AND eventType = ?")
            args.add(it.name)
        }
        eventStatus?.let {
            queryBuilder.append(" AND status = ?")
            args.add(it.name)
        }
        startTime?.let {
            queryBuilder.append(" AND creationTimestamp >= ?")
            args.add(it)
        }
        endTime?.let {
            queryBuilder.append(" AND creationTimestamp <= ?")
            args.add(it)
        }

        queryBuilder.append(" ORDER BY creationTimestamp DESC")
        
        return campusEventDao.getFilteredEvents(
            SimpleSQLiteQuery(queryBuilder.toString(), args.toTypedArray())
        ).flowOn(dispatcher)
    }

    // ==================== 网络请求方法 ====================

    override suspend fun getAllEventsNetwork(): Result<List<CampusEvent>> {
        return withContext(dispatcher) {
            try {
                val response = apiService.getAllEvents()
                response.handleResponse()
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getEventByIdNetwork(eventId: String): Result<CampusEvent> {
        return withContext(dispatcher) {
            try {
                val response = apiService.getEventById(eventId)
                response.handleResponse()
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun createEventNetwork(
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
    ): Result<CampusEvent> {
        return withContext(dispatcher) {
            try {
                val request = ApiService.CreateEventRequest(
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
                val response = apiService.createEvent(request)
                response.handleResponse()
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateEventNetwork(
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
    ): Result<CampusEvent> {
        return withContext(dispatcher) {
            try {
                val request = ApiService.UpdateEventRequest(
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
                val response = apiService.updateEvent(eventId, request)
                response.handleResponse()
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteEventNetwork(eventId: String): Result<Unit> {
        return withContext(dispatcher) {
            try {
                apiService.deleteEvent(eventId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getEventsByTypeNetwork(eventType: String): Result<List<CampusEvent>> {
        return withContext(dispatcher) {
            try {
                val response = apiService.getEventsByType(eventType)
                response.handleResponse()
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getPendingEventsNetwork(): Result<List<CampusEvent>> {
        return withContext(dispatcher) {
            try {
                val response = apiService.getPendingEvents()
                response.handleResponse()
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // [MODIFIED] 重写 getApprovedEventsNetwork 方法，让它调用我们新的 API
    override suspend fun getApprovedEventsNetwork(category: String?): Result<List<CampusEvent>> {
        return withContext(dispatcher) {
            try {
                // 调用NetworkEventRepository的getPoisAsCampusEvents方法，传入分类参数
                val campusEventsResult = networkEventRepository.getPoisAsCampusEvents(category)
                campusEventsResult
            } catch (e: Exception) {
                // 如果网络请求失败，返回一个包含错误信息的 Failure
                Result.failure(e)
            }
        }
    }

    override suspend fun uploadEventImage(eventId: String, imageBytes: ByteArray, fileName: String): Result<String> {
        return withContext(dispatcher) {
            try {
                // 暂时返回成功，实际实现需要处理图片上传
                Result.success("upload_success")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun searchEvents(query: String): Result<List<CampusEvent>> {
        return withContext(dispatcher) {
            try {
                // 暂时返回空列表，实际实现需要调用搜索API
                Result.success(emptyList())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getNearbyEvents(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Result<List<CampusEvent>> {
        return withContext(dispatcher) {
            try {
                // 暂时返回空列表，实际实现需要调用附近事件API
                Result.success(emptyList())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getPoisFromNetwork(): Result<List<CampusEvent>> {
        return withContext(dispatcher) {
            try {
                // 调用NetworkEventRepository的getPoisAsCampusEvents方法
                val campusEventsResult = networkEventRepository.getPoisAsCampusEvents()
                campusEventsResult
            } catch (e: Exception) {
                // 如果网络请求失败，返回一个包含错误信息的 Failure
                Result.failure(e)
            }
        }
    }
}

// 注意：数据转换逻辑已移至 DataMapper.kt 文件中
// 这样可以保持代码的整洁和单一职责原则