package com.example.kdyaimap.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kdyaimap.core.model.TradingCategory
import com.example.kdyaimap.core.model.ItemCondition
import com.example.kdyaimap.core.model.ContactType
import com.example.kdyaimap.core.model.TradingPost
import com.example.kdyaimap.core.model.getDisplayName
import com.example.kdyaimap.ui.viewmodel.TradingViewModel
import com.example.kdyaimap.ui.viewmodel.TradingState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingScreen(
    tradingViewModel: TradingViewModel = hiltViewModel(),
    onNavigateToCreatePost: () -> Unit,
    onNavigateToPostDetail: (TradingPost) -> Unit
) {
    val tradingState by tradingViewModel.tradingState.collectAsState()
    val selectedCategory by tradingViewModel.selectedCategory.collectAsState()
    val context = LocalContext.current
    
    // 定义交易分类
    val categories = listOf(
        "全部" to null,
        "电子产品" to TradingCategory.ELECTRONICS,
        "图书教材" to TradingCategory.BOOKS,
        "服装鞋包" to TradingCategory.CLOTHING,
        "生活用品" to TradingCategory.DAILY_NECESSITIES,
        "运动器材" to TradingCategory.SPORTS_GOODS,
        "交通工具" to TradingCategory.TRANSPORTATION,
        "数码产品" to TradingCategory.DIGITAL_GOODS,
        "家具家电" to TradingCategory.FURNITURE,
        "票券卡证" to TradingCategory.TICKETS,
        "服务技能" to TradingCategory.SERVICES,
        "其他" to TradingCategory.OTHERS
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "校园二手市场",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = onNavigateToCreatePost,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("发布")
            }
        }
        
        // 分类标签栏
        ScrollableTabRow(
            selectedTabIndex = categories.indexOfFirst { it.second == selectedCategory },
            edgePadding = 8.dp
        ) {
            categories.forEach { (title, categoryValue) ->
                Tab(
                    selected = selectedCategory == categoryValue,
                    onClick = { tradingViewModel.selectCategory(categoryValue) },
                    text = { Text(title) }
                )
            }
        }
        
        // 帖子列表
        when (val state = tradingState) {
            is TradingState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is TradingState.Success -> {
                if (state.posts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "暂无相关商品",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.posts, key = { it.id }) { post ->
                            TradingPostCard(
                                post = post,
                                onClick = { onNavigateToPostDetail(post) },
                                onLike = { tradingViewModel.likePost(post.id) }
                            )
                        }
                    }
                }
            }
            is TradingState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { tradingViewModel.loadPosts(selectedCategory) }) {
                            Text("重试")
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun TradingPostCard(
    post: TradingPost,
    onClick: () -> Unit,
    onLike: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题和价格
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "¥${post.price}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    if (post.isNegotiable) {
                        Text(
                            text = "可议价",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 分类和状况
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = post.category.getDisplayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                
                Text(
                    text = post.condition.getDisplayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 描述
            if (post.description.isNotEmpty()) {
                Text(
                    text = post.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // 图片预览
            if (post.images.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(120.dp)
                ) {
                    items(post.images.take(3)) { imageUrl ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "商品图片",
                            modifier = Modifier
                                .width(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // 底部信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (post.isAnonymous) "匿名用户" else post.authorName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = post.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 浏览次数
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = post.viewCount.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // 点赞按钮
                    IconButton(
                        onClick = onLike,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "点赞",
                            modifier = Modifier.size(20.dp),
                            tint = if (post.likeCount > 0) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (post.likeCount > 0) {
                        Text(
                            text = post.likeCount.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 时间
            Text(
                text = formatTime(post.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}