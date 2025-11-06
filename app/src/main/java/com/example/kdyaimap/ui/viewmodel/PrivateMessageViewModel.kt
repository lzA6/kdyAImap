package com.example.kdyaimap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kdyaimap.core.model.PrivateMessage
import com.example.kdyaimap.core.model.Conversation
import com.example.kdyaimap.core.model.Friendship
import com.example.kdyaimap.core.model.MessageType
import com.example.kdyaimap.core.model.FriendshipStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivateMessageViewModel @Inject constructor(
    // TODO: 注入PrivateMessageRepository
) : ViewModel() {

    private val _conversationState = MutableStateFlow<ConversationState>(ConversationState.Idle)
    val conversationState: StateFlow<ConversationState> = _conversationState.asStateFlow()

    private val _messageState = MutableStateFlow<MessageState>(MessageState.Idle)
    val messageState: StateFlow<MessageState> = _messageState.asStateFlow()

    private val _friendshipState = MutableStateFlow<FriendshipState>(FriendshipState.Idle)
    val friendshipState: StateFlow<FriendshipState> = _friendshipState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        loadConversations()
        loadFriendRequests()
    }

    // 加载会话列表
    fun loadConversations() {
        viewModelScope.launch {
            _conversationState.value = ConversationState.Loading
            try {
                // TODO: 从Repository获取会话数据
                val conversations = getMockConversations()
                _conversationState.value = ConversationState.Success(conversations)
                updateUnreadCount(conversations)
            } catch (e: Exception) {
                _conversationState.value = ConversationState.Error("加载失败: ${e.message}")
            }
        }
    }

    // 加载指定会话的消息
    fun loadMessages(conversationId: Long) {
        viewModelScope.launch {
            _messageState.value = MessageState.Loading
            try {
                // TODO: 从Repository获取消息数据
                val messages = getMockMessages(conversationId)
                _messageState.value = MessageState.Success(messages)
            } catch (e: Exception) {
                _messageState.value = MessageState.Error("加载失败: ${e.message}")
            }
        }
    }

    // 发送消息
    fun sendMessage(
        conversationId: Long,
        senderId: Long,
        senderName: String,
        receiverId: Long,
        receiverName: String,
        content: String,
        messageType: MessageType = MessageType.TEXT
    ) {
        viewModelScope.launch {
            try {
                val newMessage = PrivateMessage(
                    id = System.currentTimeMillis(), // 临时ID
                    senderId = senderId,
                    senderName = senderName,
                    receiverId = receiverId,
                    receiverName = receiverName,
                    content = content,
                    messageType = messageType,
                    createdAt = System.currentTimeMillis()
                )
                
                // TODO: 保存到Repository
                
                // 更新本地状态
                val currentState = _messageState.value
                if (currentState is MessageState.Success) {
                    val updatedMessages = currentState.messages + newMessage
                    _messageState.value = MessageState.Success(updatedMessages)
                }
                
                // TODO: 发送推送通知给接收者
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    // 标记消息为已读
    fun markMessagesAsRead(conversationId: Long) {
        viewModelScope.launch {
            try {
                // TODO: 调用Repository标记消息为已读
                
                // 更新本地状态
                val currentState = _conversationState.value
                if (currentState is ConversationState.Success) {
                    val updatedConversations = currentState.conversations.map { conversation ->
                        if (conversation.id == conversationId) {
                            conversation.copy(unreadCount = 0)
                        } else {
                            conversation
                        }
                    }
                    _conversationState.value = ConversationState.Success(updatedConversations)
                    updateUnreadCount(updatedConversations)
                }
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    // 加载好友请求
    fun loadFriendRequests() {
        viewModelScope.launch {
            _friendshipState.value = FriendshipState.Loading
            try {
                // TODO: 从Repository获取好友请求数据
                val friendships = getMockFriendships()
                _friendshipState.value = FriendshipState.Success(friendships)
            } catch (e: Exception) {
                _friendshipState.value = FriendshipState.Error("加载失败: ${e.message}")
            }
        }
    }

    // 发送好友请求
    fun sendFriendRequest(
        userId: Long,
        targetUserId: Long,
        targetUserName: String
    ) {
        viewModelScope.launch {
            try {
                val newFriendship = Friendship(
                    id = System.currentTimeMillis(), // 临时ID
                    userId = userId,
                    friendId = targetUserId,
                    friendName = targetUserName,
                    status = FriendshipStatus.PENDING
                )
                
                // TODO: 保存到Repository
                
                // 更新本地状态
                val currentState = _friendshipState.value
                if (currentState is FriendshipState.Success) {
                    val updatedFriendships = currentState.friendships + newFriendship
                    _friendshipState.value = FriendshipState.Success(updatedFriendships)
                }
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    // 处理好友请求
    fun handleFriendRequest(
        friendshipId: Long,
        accept: Boolean
    ) {
        viewModelScope.launch {
            try {
                // TODO: 调用Repository处理好友请求
                
                // 更新本地状态
                val currentState = _friendshipState.value
                if (currentState is FriendshipState.Success) {
                    val updatedFriendships = currentState.friendships.map { friendship ->
                        if (friendship.id == friendshipId) {
                            friendship.copy(
                                status = if (accept) FriendshipStatus.ACCEPTED else FriendshipStatus.DECLINED,
                                acceptedAt = if (accept) System.currentTimeMillis() else null
                            )
                        } else {
                            friendship
                        }
                    }
                    _friendshipState.value = FriendshipState.Success(updatedFriendships)
                }
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    private fun updateUnreadCount(conversations: List<Conversation>) {
        _unreadCount.value = conversations.sumOf { it.unreadCount }
    }

    // 临时模拟数据
    private fun getMockConversations(): List<Conversation> {
        return listOf(
            Conversation(
                id = 1,
                participantId = 2,
                participantName = "张同学",
                participantAvatar = null,
                lastMessage = PrivateMessage(
                    id = 1,
                    senderId = 2,
                    senderName = "张同学",
                    receiverId = 1,
                    receiverName = "我",
                    content = "这个iPhone还在吗？",
                    createdAt = System.currentTimeMillis() - 1000 * 60 * 5
                ),
                unreadCount = 2,
                isPinned = true
            ),
            Conversation(
                id = 2,
                participantId = 3,
                participantName = "李同学",
                participantAvatar = null,
                lastMessage = PrivateMessage(
                    id = 2,
                    senderId = 1,
                    senderName = "我",
                    receiverId = 3,
                    receiverName = "李同学",
                    content = "好的，谢谢！",
                    createdAt = System.currentTimeMillis() - 1000 * 60 * 30
                ),
                unreadCount = 0
            )
        )
    }

    private fun getMockMessages(conversationId: Long): List<PrivateMessage> {
        return listOf(
            PrivateMessage(
                id = 1,
                senderId = 2,
                senderName = "张同学",
                receiverId = 1,
                receiverName = "我",
                content = "你好，看到你发布的iPhone，还在吗？",
                createdAt = System.currentTimeMillis() - 1000 * 60 * 10
            ),
            PrivateMessage(
                id = 2,
                senderId = 1,
                senderName = "我",
                receiverId = 2,
                receiverName = "张同学",
                content = "在的，你需要吗？",
                createdAt = System.currentTimeMillis() - 1000 * 60 * 8
            ),
            PrivateMessage(
                id = 3,
                senderId = 2,
                senderName = "张同学",
                receiverId = 1,
                receiverName = "我",
                content = "这个iPhone还在吗？",
                createdAt = System.currentTimeMillis() - 1000 * 60 * 5
            )
        )
    }

    private fun getMockFriendships(): List<Friendship> {
        return listOf(
            Friendship(
                id = 1,
                userId = 1,
                friendId = 4,
                friendName = "王同学",
                status = FriendshipStatus.PENDING,
                requestedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 2
            ),
            Friendship(
                id = 2,
                userId = 5,
                friendId = 1,
                friendName = "赵同学",
                status = FriendshipStatus.PENDING,
                requestedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 5
            )
        )
    }
}

sealed class ConversationState {
    object Idle : ConversationState()
    object Loading : ConversationState()
    data class Success(val conversations: List<Conversation>) : ConversationState()
    data class Error(val message: String) : ConversationState()
}

sealed class MessageState {
    object Idle : MessageState()
    object Loading : MessageState()
    data class Success(val messages: List<PrivateMessage>) : MessageState()
    data class Error(val message: String) : MessageState()
}

sealed class FriendshipState {
    object Idle : FriendshipState()
    object Loading : FriendshipState()
    data class Success(val friendships: List<Friendship>) : FriendshipState()
    data class Error(val message: String) : FriendshipState()
}