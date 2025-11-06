package com.example.kdyaimap.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kdyaimap.core.model.NotificationType
import com.example.kdyaimap.ui.viewmodel.NotificationViewModel
import com.example.kdyaimap.ui.viewmodel.NotificationState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val notificationState by notificationViewModel.notificationState.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "消息通知",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            // 全部标记为已读按钮
            if (unreadCount > 0) {
                TextButton(
                    onClick = { notificationViewModel.markAllAsRead() }
                ) {
                    Text("全部已读")
                }
            }
        }
        
        // 通知列表
        when (val state = notificationState) {
            is NotificationState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is NotificationState.Success -> {
                if (state.notifications.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.NotificationsNone,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "暂无通知",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.notifications, key = { it.id }) { notification ->
                            NotificationItem(
                                notification = notification,
                                onMarkAsRead = { notificationViewModel.markAsRead(notification.id) },
                                onDelete = { notificationViewModel.deleteNotification(notification.id) }
                            )
                        }
                    }
                }
            }
            is NotificationState.Error -> {
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
                        Button(onClick = { notificationViewModel.loadNotifications() }) {
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
private fun NotificationItem(
    notification: com.example.kdyaimap.core.model.Notification,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                if (!notification.isRead) {
                    onMarkAsRead()
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!notification.isRead) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 顶部行：图标、标题、时间、删除按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // 通知类型图标
                    Icon(
                        getNotificationIcon(notification.type),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = getNotificationColor(notification.type)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        // 标题
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // 时间
                        Text(
                            text = formatTime(notification.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // 未读指示器
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
                
                // 删除按钮
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "删除",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 内容
            Text(
                text = notification.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            // 相关用户信息
            notification.relatedUserName?.let { userName ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "来自: $userName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除通知") },
            text = { Text("确定要删除这条通知吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun getNotificationIcon(type: NotificationType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        NotificationType.COMMENT_REPLY -> Icons.AutoMirrored.Filled.Reply
        NotificationType.POST_LIKE -> Icons.Default.Favorite
        NotificationType.COMMENT_LIKE -> Icons.Default.ThumbUp
        NotificationType.POST_SOLD -> Icons.Default.ShoppingCart
        NotificationType.SYSTEM_NOTICE -> Icons.Default.Notifications
    }
}

@Composable
private fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.COMMENT_REPLY -> MaterialTheme.colorScheme.primary
        NotificationType.POST_LIKE -> MaterialTheme.colorScheme.error
        NotificationType.COMMENT_LIKE -> MaterialTheme.colorScheme.secondary
        NotificationType.POST_SOLD -> MaterialTheme.colorScheme.tertiary
        NotificationType.SYSTEM_NOTICE -> MaterialTheme.colorScheme.outline
    }
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "刚刚"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}天前"
        else -> {
            val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}