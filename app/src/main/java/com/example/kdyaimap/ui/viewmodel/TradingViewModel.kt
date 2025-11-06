package com.example.kdyaimap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kdyaimap.core.model.TradingPost
import com.example.kdyaimap.core.model.TradingCategory
import com.example.kdyaimap.core.model.ItemCondition
import com.example.kdyaimap.core.model.ContactType
import com.example.kdyaimap.core.model.PostStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TradingViewModel @Inject constructor(
    // TODO: 注入相关的Repository
) : ViewModel() {

    private val _tradingState = MutableStateFlow<TradingState>(TradingState.Idle)
    val tradingState: StateFlow<TradingState> = _tradingState.asStateFlow()

    private val _selectedCategory = MutableStateFlow<TradingCategory?>(null)
    val selectedCategory: StateFlow<TradingCategory?> = _selectedCategory.asStateFlow()

    private val _selectedPost = MutableStateFlow<TradingPost?>(null)
    val selectedPost: StateFlow<TradingPost?> = _selectedPost.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts(category: TradingCategory? = null) {
        viewModelScope.launch {
            _tradingState.value = TradingState.Loading
            try {
                // TODO: 从Repository获取帖子数据
                val posts = getMockPosts(category)
                _tradingState.value = TradingState.Success(posts)
            } catch (e: Exception) {
                _tradingState.value = TradingState.Error("加载失败: ${e.message}")
            }
        }
    }

    fun selectCategory(category: TradingCategory?) {
        _selectedCategory.value = category
        loadPosts(category)
    }

    fun selectPost(post: TradingPost) {
        _selectedPost.value = post
        // 增加浏览次数
        incrementViewCount(post.id)
    }

    fun createPost(
        title: String,
        description: String,
        category: TradingCategory,
        price: Double,
        isNegotiable: Boolean,
        condition: ItemCondition,
        images: List<String>,
        location: String,
        latitude: Double,
        longitude: Double,
        contactInfo: String,
        contactType: ContactType,
        authorId: Long,
        authorName: String,
        isAnonymous: Boolean,
        tags: List<String>
    ) {
        viewModelScope.launch {
            _tradingState.value = TradingState.Loading
            try {
                val newPost = TradingPost(
                    title = title,
                    description = description,
                    category = category,
                    price = price,
                    isNegotiable = isNegotiable,
                    condition = condition,
                    images = images,
                    location = location,
                    latitude = latitude,
                    longitude = longitude,
                    contactInfo = contactInfo,
                    contactType = contactType,
                    authorId = authorId,
                    authorName = authorName,
                    isAnonymous = isAnonymous,
                    tags = tags
                )
                
                // TODO: 保存到Repository
                _tradingState.value = TradingState.PostCreated(newPost)
                loadPosts(_selectedCategory.value) // 重新加载列表
            } catch (e: Exception) {
                _tradingState.value = TradingState.Error("发布失败: ${e.message}")
            }
        }

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            try {
                // TODO: 调用Repository删除帖子
                loadPosts(_selectedCategory.value) // 重新加载列表
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    fun incrementViewCount(postId: Long) {
        viewModelScope.launch {
            try {
                // TODO: 调用Repository增加浏览次数
                // 更新本地状态
                val currentState = _tradingState.value
                if (currentState is TradingState.Success) {
                    val updatedPosts = currentState.posts.map { post ->
                        if (post.id == postId) {
                            post.copy(viewCount = post.viewCount + 1)
                        } else {
                            post
                        }
                    }
                    _tradingState.value = TradingState.Success(updatedPosts)
                }
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
    }

    fun likePost(postId: Long) {
        viewModelScope.launch {
            try {
                // TODO: 调用Repository增加点赞数
                loadPosts(_selectedCategory.value) // 重新加载列表
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    fun markAsSold(postId: Long) {
        viewModelScope.launch {
            try {
                // TODO: 调用Repository标记为已售出
                loadPosts(_selectedCategory.value) // 重新加载列表
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    private fun incrementViewCount(postId: Long) {
        viewModelScope.launch {
            try {
                // TODO: 调用Repository增加浏览次数
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    // 临时模拟数据，实际应该从Repository获取
    private fun getMockPosts(category: TradingCategory?): List<TradingPost> {
        val allPosts = listOf(
            TradingPost(
                id = 1,
                title = "iPhone 13 Pro 256GB",
                description = "个人自用iPhone 13 Pro，256GB，远峰蓝色，无磕碰，功能完好，配件齐全。",
                category = TradingCategory.ELECTRONICS,
                price = 4999.0,
                isNegotiable = true,
                condition = ItemCondition.LIKE_NEW,
                images = listOf("iphone1.jpg", "iphone2.jpg"),
                location = "学生宿舍A栋",
                latitude = 39.9042,
                longitude = 116.4074,
                contactInfo = "13812345678",
                contactType = ContactType.PHONE,
                authorId = 1,
                authorName = "张同学",
                isAnonymous = false,
                viewCount = 156,
                likeCount = 23,
                tags = listOf("苹果", "手机", "电子产品")
            ),
            TradingPost(
                id = 2,
                title = "高等数学教材全套",
                description = "同济版高等数学教材，上下册，几乎全新，适合大一新生。",
                category = TradingCategory.BOOKS,
                price = 35.0,
                isNegotiable = false,
                condition = ItemCondition.BRAND_NEW,
                images = listOf("math_book.jpg"),
                location = "图书馆",
                latitude = 39.9052,
                longitude = 116.4084,
                contactInfo = "wx_zhangsan",
                contactType = ContactType.WECHAT,
                authorId = 2,
                authorName = "李同学",
                isAnonymous = true,
                viewCount = 89,
                likeCount = 12,
                tags = listOf("教材", "数学", "新书")
            ),
            TradingPost(
                id = 3,
                title = "捷安特山地车",
                description = "21速捷安特山地车，九成新，适合校园骑行，轮胎很新。",
                category = TradingCategory.TRANSPORTATION,
                price = 280.0,
                isNegotiable = true,
                condition = ItemCondition.GOOD,
                images = listOf("bike1.jpg", "bike2.jpg", "bike3.jpg"),
                location = "自行车棚",
                latitude = 39.9032,
                longitude = 116.4064,
                contactInfo = "面议",
                contactType = ContactType.IN_PERSON,
                authorId = 3,
                authorName = "王同学",
                isAnonymous = false,
                viewCount = 234,
                likeCount = 45,
                tags = listOf("自行车", "山地车", "交通工具")
            )
        )

        return if (category != null) {
            allPosts.filter { it.category == category }
        } else {
            allPosts
        }
    }
}

sealed class TradingState {
    object Idle : TradingState()
    object Loading : TradingState()
    data class Success(val posts: List<TradingPost>) : TradingState()
    data class PostCreated(val post: TradingPost) : TradingState()
    data class Error(val message: String) : TradingState()
}