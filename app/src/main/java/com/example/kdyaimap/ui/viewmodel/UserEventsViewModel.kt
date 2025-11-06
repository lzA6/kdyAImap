package com.example.kdyaimap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kdyaimap.core.data.repository.UserPreferencesRepository
import com.example.kdyaimap.domain.repository.CampusEventRepository
import com.example.kdyaimap.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserEventsViewModel @Inject constructor(
    private val campusEventRepository: CampusEventRepository,
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _userEventsState = MutableStateFlow<UserEventsState>(UserEventsState.Loading)
    val userEventsState: StateFlow<UserEventsState> = _userEventsState.asStateFlow()

    /**
     * 加载用户活动（发布的和参与的）
     */
    fun loadUserEvents() {
        viewModelScope.launch {
            userPreferencesRepository.userId.first()?.let { userId ->
                _userEventsState.value = UserEventsState.Loading
                
                try {
                    // 并行加载发布的活动和参与的活动
                    val publishedDeferred = async { loadPublishedEvents(userId.toString()) }
                    val participatedDeferred = async { loadParticipatedEvents(userId.toString()) }
                    
                    val publishedEvents = publishedDeferred.await()
                    val participatedEvents = participatedDeferred.await()
                    
                    _userEventsState.value = UserEventsState.Success(
                        publishedEvents = publishedEvents,
                        participatedEvents = participatedEvents
                    )
                } catch (e: Exception) {
                    _userEventsState.value = UserEventsState.Error("加载用户活动失败：${e.message}")
                }
            } ?: run {
                _userEventsState.value = UserEventsState.Error("用户未登录")
            }
        }
    }

    /**
     * 加载用户发布的活动
     */
    private suspend fun loadPublishedEvents(userId: String): List<com.example.kdyaimap.core.model.CampusEvent> {
        return try {
            // 先尝试从网络获取
            val result = campusEventRepository.getApprovedEventsNetwork()
            result.getOrNull()?.filter { it.authorId.toString() == userId } ?: emptyList()
        } catch (e: Exception) {
            // 异常时返回空列表
            emptyList()
        }
    }

    /**
     * 加载用户参与的活动
     */
    private suspend fun loadParticipatedEvents(userId: String): List<com.example.kdyaimap.core.model.CampusEvent> {
        return try {
            // 先尝试从网络获取
            val result = campusEventRepository.getApprovedEventsNetwork()
            result.getOrNull()?.filter {
                // 这里需要根据实际的参与逻辑来过滤
                // 暂时返回空列表，需要根据业务逻辑实现
                false
            } ?: emptyList()
        } catch (e: Exception) {
            // 异常时返回空列表
            emptyList()
        }
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        loadUserEvents()
    }

    /**
     * 取消活动
     */
    fun cancelEvent(eventId: String) {
        viewModelScope.launch {
            try {
                val result = campusEventRepository.updateEventNetwork(
                    eventId = eventId,
                    status = "CANCELLED",
                    title = null,
                    description = null,
                    eventType = null,
                    location = null,
                    latitude = null,
                    longitude = null,
                    startTime = null,
                    endTime = null,
                    maxParticipants = null,
                    images = null
                )
                if (result.isSuccess) {
                    // 刷新数据
                    loadUserEvents()
                }
            } catch (e: Exception) {
                // 处理异常
            }
        }
    }

    /**
     * 退出活动
     */
    fun leaveEvent(eventId: String) {
        viewModelScope.launch {
            userPreferencesRepository.userId.first()?.let { userId ->
                try {
                    // 这里需要实现退出活动的逻辑
                    // 暂时直接刷新数据
                    loadUserEvents()
                } catch (e: Exception) {
                    // 处理异常
                }
            }
        }
    }
}

sealed class UserEventsState {
    object Loading : UserEventsState()
    data class Success(
        val publishedEvents: List<com.example.kdyaimap.core.model.CampusEvent>,
        val participatedEvents: List<com.example.kdyaimap.core.model.CampusEvent>
    ) : UserEventsState()
    data class Error(val message: String) : UserEventsState()
}