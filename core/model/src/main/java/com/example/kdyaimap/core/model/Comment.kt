package com.example.kdyaimap.core.model

data class Comment(
    val id: Long,
    val postId: Long,
    val authorId: Long,
    val authorName: String,
    val authorAvatar: String? = null,
    val content: String,
    val createdAt: Long,
    val likeCount: Int = 0,
    val isAnonymous: Boolean = false,
    val parentCommentId: Long? = null, // 用于回复评论
    val replies: List<Comment> = emptyList(),
    val isDeleted: Boolean = false
)

data class CommentReply(
    val id: Long,
    val commentId: Long,
    val authorId: Long,
    val authorName: String,
    val authorAvatar: String? = null,
    val content: String,
    val createdAt: Long,
    val likeCount: Int = 0,
    val isAnonymous: Boolean = false,
    val replyToUserId: Long? = null, // 回复给哪个用户
    val replyToUserName: String? = null,
    val isDeleted: Boolean = false
)

// 消息通知模型
data class Notification(
    val id: Long,
    val userId: Long,
    val type: NotificationType,
    val title: String,
    val content: String,
    val relatedId: Long, // 相关的帖子ID、评论ID等
    val relatedUserId: Long? = null, // 触发通知的用户ID
    val relatedUserName: String? = null,
    val createdAt: Long,
    val isRead: Boolean = false,
    val extraData: Map<String, String> = emptyMap()
)

enum class NotificationType {
    COMMENT_REPLY, // 评论回复
    POST_LIKE,     // 帖子点赞
    COMMENT_LIKE,  // 评论点赞
    POST_SOLD,     // 帖子已售出
    SYSTEM_NOTICE  // 系统通知
}

fun NotificationType.getDisplayName(): String {
    return when (this) {
        NotificationType.COMMENT_REPLY -> "评论回复"
        NotificationType.POST_LIKE -> "帖子点赞"
        NotificationType.COMMENT_LIKE -> "评论点赞"
        NotificationType.POST_SOLD -> "商品已售出"
        NotificationType.SYSTEM_NOTICE -> "系统通知"
    }
}