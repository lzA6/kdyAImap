package com.example.kdyaimap.core.model

import java.util.*

data class PrivateMessage(
    val id: Long,
    val senderId: Long,
    val senderName: String,
    val senderAvatar: String? = null,
    val receiverId: Long,
    val receiverName: String,
    val receiverAvatar: String? = null,
    val content: String,
    val messageType: MessageType = MessageType.TEXT,
    val imageUrl: String? = null, // 图片消息的URL
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isDeleted: Boolean = false
)

data class Conversation(
    val id: Long,
    val participantId: Long,
    val participantName: String,
    val participantAvatar: String? = null,
    val lastMessage: PrivateMessage?,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

data class Friendship(
    val id: Long,
    val userId: Long,
    val friendId: Long,
    val friendName: String,
    val friendAvatar: String? = null,
    val status: FriendshipStatus,
    val requestedAt: Long = System.currentTimeMillis(),
    val acceptedAt: Long? = null
)

enum class MessageType {
    TEXT,           // 文本消息
    IMAGE,          // 图片消息
    VOICE,          // 语音消息
    SYSTEM          // 系统消息
}

enum class FriendshipStatus {
    PENDING,        // 待确认
    ACCEPTED,       // 已接受
    DECLINED,       // 已拒绝
    BLOCKED         // 已拉黑
}

fun MessageType.getDisplayName(): String {
    return when (this) {
        MessageType.TEXT -> "文本"
        MessageType.IMAGE -> "图片"
        MessageType.VOICE -> "语音"
        MessageType.SYSTEM -> "系统"
    }
}

fun FriendshipStatus.getDisplayName(): String {
    return when (this) {
        FriendshipStatus.PENDING -> "待确认"
        FriendshipStatus.ACCEPTED -> "好友"
        FriendshipStatus.DECLINED -> "已拒绝"
        FriendshipStatus.BLOCKED -> "已拉黑"
    }
}