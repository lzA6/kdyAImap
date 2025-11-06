package com.example.kdyaimap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kdyaimap.core.model.Comment
import com.example.kdyaimap.core.model.CommentReply
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    // TODO: 注入CommentRepository
) : ViewModel() {

    private val _commentState = MutableStateFlow<CommentState>(CommentState.Idle)
    val commentState: StateFlow<CommentState> = _commentState.asStateFlow()

    private val _selectedPostId = MutableStateFlow<Long?>(null)
    val selectedPostId: StateFlow<Long?> = _selectedPostId.asStateFlow()

    fun loadComments(postId: Long) {
        _selectedPostId.value = postId
        viewModelScope.launch {
            _commentState.value = CommentState.Loading
            try {
                // TODO: 从Repository获取评论数据
                val comments = getMockComments(postId)
                _commentState.value = CommentState.Success(comments)
            } catch (e: Exception) {
                _commentState.value = CommentState.Error("加载失败: ${e.message}")
            }
        }
    }

    fun addComment(
        postId: Long,
        content: String,
        authorId: Long,
        authorName: String,
        isAnonymous: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                val newComment = Comment(
                    id = System.currentTimeMillis(), // 临时ID
                    postId = postId,
                    authorId = authorId,
                    authorName = if (isAnonymous) "匿名用户" else authorName,
                    content = content,
                    createdAt = System.currentTimeMillis(),
                    isAnonymous = isAnonymous
                )
                
                // TODO: 保存到Repository
                
                // 更新本地状态
                val currentState = _commentState.value
                if (currentState is CommentState.Success) {
                    val updatedComments = listOf(newComment) + currentState.comments
                    _commentState.value = CommentState.Success(updatedComments)
                }
                
                // TODO: 发送通知给帖子作者
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    fun replyToComment(
        commentId: Long,
        content: String,
        authorId: Long,
        authorName: String,
        replyToUserId: Long? = null,
        replyToUserName: String? = null,
        isAnonymous: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                val newReply = CommentReply(
                    id = System.currentTimeMillis(), // 临时ID
                    commentId = commentId,
                    authorId = authorId,
                    authorName = if (isAnonymous) "匿名用户" else authorName,
                    content = content,
                    createdAt = System.currentTimeMillis(),
                    isAnonymous = isAnonymous,
                    replyToUserId = replyToUserId,
                    replyToUserName = replyToUserName
                )
                
                // TODO: 保存到Repository
                
                // 更新本地状态
                val currentState = _commentState.value
                if (currentState is CommentState.Success) {
                    val updatedComments = currentState.comments.map { comment ->
                        if (comment.id == commentId) {
                            // 将CommentReply转换为Comment并添加到replies中
                            val replyAsComment = Comment(
                                id = newReply.id,
                                postId = comment.postId,
                                authorId = newReply.authorId,
                                authorName = newReply.authorName,
                                authorAvatar = newReply.authorAvatar,
                                content = newReply.content,
                                createdAt = newReply.createdAt,
                                likeCount = newReply.likeCount,
                                isAnonymous = newReply.isAnonymous,
                                parentCommentId = commentId,
                                isDeleted = newReply.isDeleted
                            )
                            comment.copy(
                                replies = comment.replies + replyAsComment
                            )
                        } else comment
                    }
                    _commentState.value = CommentState.Success(updatedComments)
                }
                
                // TODO: 发送通知给被回复的用户
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    fun likeComment(commentId: Long) {
        viewModelScope.launch {
            try {
                // TODO: 调用Repository点赞评论
                
                // 更新本地状态
                val currentState = _commentState.value
                if (currentState is CommentState.Success) {
                    val updatedComments = currentState.comments.map { comment ->
                        if (comment.id == commentId) {
                            comment.copy(likeCount = comment.likeCount + 1)
                        } else {
                            comment
                        }
                    }
                    _commentState.value = CommentState.Success(updatedComments)
                }
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    fun deleteComment(commentId: Long) {
        viewModelScope.launch {
            try {
                // TODO: 调用Repository删除评论
                
                // 更新本地状态
                val currentState = _commentState.value
                if (currentState is CommentState.Success) {
                    val updatedComments = currentState.comments.map { comment ->
                        if (comment.id == commentId) {
                            comment.copy(isDeleted = true)
                        } else comment
                    } as List<Comment>
                    _commentState.value = CommentState.Success(updatedComments)
                }
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    // 临时模拟数据，实际应该从Repository获取
    private fun getMockComments(postId: Long): List<Comment> {
        return listOf(
            Comment(
                id = 1,
                postId = postId,
                authorId = 2,
                authorName = "张同学",
                content = "这个价格怎么样？可以便宜点吗？",
                createdAt = System.currentTimeMillis() - 1000 * 60 * 30, // 30分钟前
                likeCount = 3,
                isAnonymous = false,
                replies = listOf(
                    Comment(
                        id = 11,
                        postId = postId,
                        authorId = 1,
                        authorName = "李同学",
                        content = "价格可以小刀，最低4500",
                        createdAt = System.currentTimeMillis() - 1000 * 60 * 25, // 25分钟前
                        likeCount = 1,
                        isAnonymous = false,
                        parentCommentId = 1
                    )
                )
            ),
            Comment(
                id = 2,
                postId = postId,
                authorId = 3,
                authorName = "王同学",
                content = "东西还在吗？什么时候可以看？",
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 2, // 2小时前
                likeCount = 1,
                isAnonymous = true
            ),
            Comment(
                id = 3,
                postId = postId,
                authorId = 4,
                authorName = "赵同学",
                content = "手机有保修吗？配件齐全吗？",
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 5, // 5小时前
                likeCount = 2,
                isAnonymous = false,
                replies = listOf(
                    Comment(
                        id = 31,
                        postId = postId,
                        authorId = 1,
                        authorName = "匿名用户",
                        content = "有保修，配件齐全，原装充电器、数据线、耳机都在",
                        createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 4, // 4小时前
                        likeCount = 0,
                        isAnonymous = true,
                        parentCommentId = 3
                    )
                )
            )
        )
    }
}

sealed class CommentState {
    object Idle : CommentState()
    object Loading : CommentState()
    data class Success(val comments: List<Comment>) : CommentState()
    data class Error(val message: String) : CommentState()
}