package com.example.kdyaimap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.core.model.User
import com.example.kdyaimap.domain.usecase.DeleteEventUseCase
import com.example.kdyaimap.domain.usecase.GetEventByIdUseCase
import com.example.kdyaimap.domain.usecase.InsertEventUseCase
import com.example.kdyaimap.domain.usecase.UpdateEventUseCase
import com.example.kdyaimap.domain.usecase.GetUserByIdUseCase
import com.example.kdyaimap.domain.repository.CampusEventRepository
import com.example.kdyaimap.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val insertEventUseCase: InsertEventUseCase,
    private val updateEventUseCase: UpdateEventUseCase,
    private val deleteEventUseCase: DeleteEventUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val campusEventRepository: CampusEventRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _eventState = MutableStateFlow<EventState>(EventState.Idle)
    val eventState: StateFlow<EventState> = _eventState.asStateFlow()

    private val _organizerState = MutableStateFlow<OrganizerState>(OrganizerState.Idle)
    val organizerState: StateFlow<OrganizerState> = _organizerState.asStateFlow()

    // ==================== 本地数据库方法 ====================

    fun loadEvent(eventId: Long) {
        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                val event = getEventByIdUseCase(eventId)
                _eventState.value = EventState.Success(event)
            } catch (e: Exception) {
                _eventState.value = EventState.Error(e.message ?: "加载活动失败")
            }
        }
    }

    fun createEvent(event: CampusEvent) {
        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                insertEventUseCase(event)
                _eventState.value = EventState.Success(null)
            } catch (e: Exception) {
                _eventState.value = EventState.Error(e.message ?: "创建活动失败")
            }
        }
    }

    fun updateEvent(event: CampusEvent) {
        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                updateEventUseCase(event)
                _eventState.value = EventState.Success(null)
            } catch (e: Exception) {
                _eventState.value = EventState.Error(e.message ?: "更新活动失败")
            }
        }
    }

    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                deleteEventUseCase(eventId)
                _eventState.value = EventState.Success(null)
            } catch (e: Exception) {
                _eventState.value = EventState.Error(e.message ?: "删除活动失败")
            }
        }
    }

    // ==================== 网络请求方法 ====================

    /**
     * 网络创建活动
     */
    fun createEventNetwork(
        title: String,
        description: String,
        eventType: String,
        location: String,
        latitude: Double,
        longitude: Double,
        startTime: Long,
        endTime: Long? = null,
        maxParticipants: Int? = null,
        images: List<String>? = null,
        organizerId: String
    ) {
        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                val result = campusEventRepository.createEventNetwork(
                    title = title,
                    description = description,
                    eventType = eventType,
                    location = location,
                    latitude = latitude,
                    longitude = longitude,
                    startTime = startTime,
                    endTime = endTime ?: 0L,
                    maxParticipants = maxParticipants,
                    images = images,
                    organizerId = organizerId
                )
                
                result.onSuccess { event ->
                    _eventState.value = EventState.Success(event)
                }.onFailure { exception ->
                    _eventState.value = EventState.Error(
                        exception.message ?: "创建活动失败，请检查网络连接"
                    )
                }
            } catch (e: Exception) {
                _eventState.value = EventState.Error("创建活动失败：${e.message}")
            }
        }
    }

    /**
     * 网络更新活动
     */
    fun updateEventNetwork(
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
    ) {
        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                val result = campusEventRepository.updateEventNetwork(
                    eventId = eventId,
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
                
                result.onSuccess { event ->
                    _eventState.value = EventState.Success(event)
                }.onFailure { exception ->
                    _eventState.value = EventState.Error(
                        exception.message ?: "更新活动失败，请检查网络连接"
                    )
                }
            } catch (e: Exception) {
                _eventState.value = EventState.Error("更新活动失败：${e.message}")
            }
        }
    }

    /**
     * 网络删除活动
     */
    fun deleteEventNetwork(eventId: String) {
        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                val result = campusEventRepository.deleteEventNetwork(eventId)
                
                result.onSuccess {
                    _eventState.value = EventState.Success(null)
                }.onFailure { exception ->
                    _eventState.value = EventState.Error(
                        exception.message ?: "删除活动失败，请检查网络连接"
                    )
                }
            } catch (e: Exception) {
                _eventState.value = EventState.Error("删除活动失败：${e.message}")
            }
        }
    }

    /**
     * 网络获取活动详情
     */
    fun loadEventNetwork(eventId: String) {
        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                val result = campusEventRepository.getEventByIdNetwork(eventId)
                
                result.onSuccess { event ->
                    _eventState.value = EventState.Success(event)
                }.onFailure { exception ->
                    _eventState.value = EventState.Error(
                        exception.message ?: "加载活动失败，请检查网络连接"
                    )
                }
            } catch (e: Exception) {
                _eventState.value = EventState.Error("加载活动失败：${e.message}")
            }
        }
    }

    /**
     * 上传活动图片
     */
    fun uploadEventImage(eventId: String, imageBytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                val result = campusEventRepository.uploadEventImage(eventId, imageBytes, fileName)
                
                result.onSuccess { imageUrl ->
                    _eventState.value = EventState.ImageUploadSuccess(imageUrl)
                }.onFailure { exception ->
                    _eventState.value = EventState.Error(
                        exception.message ?: "上传图片失败，请检查网络连接"
                    )
                }
            } catch (e: Exception) {
                _eventState.value = EventState.Error("上传图片失败：${e.message}")
            }
        }
    }

    /**
     * 获取活动组织者信息
     */
    fun loadOrganizer(organizerId: Long) {
        viewModelScope.launch {
            _organizerState.value = OrganizerState.Loading
            try {
                val organizer = getUserByIdUseCase(organizerId)
                _organizerState.value = OrganizerState.Success(organizer)
            } catch (e: Exception) {
                _organizerState.value = OrganizerState.Error(e.message ?: "获取组织者信息失败")
            }
        }
    }

    /**
     * 网络获取活动组织者信息
     */
    fun loadOrganizerNetwork(organizerId: String) {
        viewModelScope.launch {
            _organizerState.value = OrganizerState.Loading
            try {
                val result = userRepository.getUserByIdNetwork(organizerId)
                
                result.onSuccess { organizer ->
                    _organizerState.value = OrganizerState.Success(organizer)
                }.onFailure { exception ->
                    _organizerState.value = OrganizerState.Error(
                        exception.message ?: "获取组织者信息失败，请检查网络连接"
                    )
                }
            } catch (e: Exception) {
                _organizerState.value = OrganizerState.Error("获取组织者信息失败：${e.message}")
            }
        }
    }

    /**
     * 重置状态
     */
    fun resetState() {
        _eventState.value = EventState.Idle
        _organizerState.value = OrganizerState.Idle
    }
}

sealed class EventState {
    object Idle : EventState()
    object Loading : EventState()
    data class Success(val event: CampusEvent?) : EventState()
    data class Error(val message: String) : EventState()
    data class ImageUploadSuccess(val imageUrl: String) : EventState()
}

sealed class OrganizerState {
    object Idle : OrganizerState()
    object Loading : OrganizerState()
    data class Success(val organizer: User?) : OrganizerState()
    data class Error(val message: String) : OrganizerState()
}