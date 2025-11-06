package com.example.kdyaimap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kdyaimap.core.model.Notification
import com.example.kdyaimap.core.model.NotificationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    // TODO: 注入NotificationRepository
) : ViewModel() {

    private val _notificationState = MutableStateFlow<NotificationState>(NotificationState.Idle)
    val notificationState: StateFlow<NotificationState> = _notificationState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _notificationState.value = NotificationState.Loading
            try {
                // TODO: 从Repository获取通知数据
                val notifications = getMockNotifications()
                _notificationState.value = NotificationState.Success(notifications)
                updateUnreadCount(notifications)
            } catch (e: Exception) {
                _notificationState.value = NotificationState.Error("加载失败: ${e.message}")
            }
        }
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            try {
                // TODO: 调用Repository标记为已读
                val currentState = _notificationState.value
                if (currentState is NotificationState.Success) {
                    val updatedNotifications = currentState.notifications.map { notification ->
                        if (notification.id == notificationId) {
                            notification.copy(isRead = true)
                        } else {
                            notification
                        }
                    }
                    _notificationState.value = NotificationState.Success(updatedNotifications)
                    updateUnreadCount(updatedNotifications)
                }
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                // TODO: 调用Repository标记所有为已读
                val currentState = _notificationState.value
                if (currentState is NotificationState.Success) {
                    val updatedNotifications = currentState.notifications.map { notification ->
                        notification.copy(isRead = true)
                    }
                    _notificationState.value = NotificationState.Success(updatedNotifications)
                    _unreadCount.value = 0
                }
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    fun deleteNotification(notificationId: Long) {
        viewModelScope.launch {
            try {
                // TODO: 调用Repository删除通知
                val currentState = _notificationState.value
                if (currentState is NotificationState.Success) {
                    val updatedNotifications = currentState.notifications.filter { it.id != notificationId }
                    _notificationState.value = NotificationState.Success(updatedNotifications)
                    updateUnreadCount(updatedNotifications)
                }
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    fun createNotification(
        userId: Long,
        type: NotificationType,
        title: String,
        content: String,
        relatedId: Long,
        relatedUserId: Long? = null,
        relatedUserName: String? = null,
        extraData: Map<String, String> = emptyMap()
    ) {
        viewModelScope.launch {
            try {
                val newNotification = Notification(
                    id = System.currentTimeMillis(), // 临时ID
                    userId = userId,
                    type = type,
                    title = title,
                    content = content,
                    relatedId = relatedId,
                    relatedUserId = relatedUserId,
                    relatedUserName = relatedUserName,
                    createdAt = System.currentTimeMillis(),
                    isRead = false,
                    extraData = extraData
                )
                
                // TODO: 保存到Repository
                
                // 更新本地状态
                val currentState = _notificationState.value
                if (currentState is NotificationState.Success) {
                    val updatedNotifications = listOf(newNotification) + currentState.notifications
                    _notificationState.value = NotificationState.Success(updatedNotifications)
                    updateUnreadCount(updatedNotifications)
                }
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    private fun updateUnreadCount(notifications: List<Notification>) {
        _unreadCount.value = notifications.count { !it.isRead }
    }

    // 临时模拟数据，实际应该从Repository获取
    private fun getMockNotifications(): List<Notification> {
        return listOf(
            Notification(
                id = 1,
                userId = 1,
                type = NotificationType.COMMENT_REPLY,
                title = "新评论回复",
                content = "张同学回复了你的帖子：iPhone 13 Pro 256GB",
                relatedId = 1,
                relatedUserId = 2,
                relatedUserName = "张同学",
                createdAt = System.currentTimeMillis() - 1000 * 60 * 5, // 5分钟前
                isRead = false
            ),
            Notification(
                id = 2,
                userId = 1,
                type = NotificationType.POST_LIKE,
                title = "帖子获赞",
                content = "李同学点赞了你的帖子：高等数学教材全套",
                relatedId = 2,
                relatedUserId = 3,
                relatedUserName = "李同学",
                createdAt = System.currentTimeMillis() - 1000 * 60 * 30, // 30分钟前
                isRead = false
            ),
            Notification(
                id = 3,
                userId = 1,
                type = NotificationType.POST_SOLD,
                title = "商品已售出",
                content = "你的帖子：捷安特山地车 已标记为售出",
                relatedId = 3,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 2, // 2小时前
                isRead = true
            ),
            Notification(
                id = 4,
                userId = 1,
                type = NotificationType.SYSTEM_NOTICE,
                title = "系统通知",
                content = "欢迎使用校园二手市场！请遵守交易规则。",
                relatedId = 0,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 24, // 1天前
                isRead = true
            )
        )
    }
}

sealed class NotificationState {
    object Idle : NotificationState()
    object Loading : NotificationState()
    data class Success(val notifications: List<Notification>) : NotificationState()
    data class Error(val message: String) : NotificationState()
}