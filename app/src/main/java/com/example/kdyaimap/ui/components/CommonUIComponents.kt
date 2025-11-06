package com.example.kdyaimap.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 通用加载状态组件
 */
@Composable
fun LoadingState(
    message: String = "加载中...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 通用错误状态组件
 */
@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            
            Text(
                text = "出错了",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                onRetry?.let { retry ->
                    Button(onClick = retry) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("重试")
                    }
                }
                
                onBack?.let { back ->
                    OutlinedButton(onClick = back) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("返回")
                    }
                }
            }
        }
    }
}

/**
 * 通用空状态组件
 */
@Composable
fun EmptyState(
    message: String,
    subtitle: String = "",
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (actionText != null && onAction != null) {
                Button(onClick = onAction) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(actionText)
                }
            }
        }
    }
}

/**
 * 通用成功状态组件
 */
@Composable
fun SuccessState(
    message: String,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            onDismiss?.let { dismiss ->
                Button(onClick = dismiss) {
                    Text("确定")
                }
            }
        }
    }
}

/**
 * 通用状态包装器
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <T> StateWrapper(
    state: UiState<T>,
    modifier: Modifier = Modifier,
    loadingMessage: String = "加载中...",
    emptyMessage: String = "暂无数据",
    emptySubtitle: String = "",
    emptyActionText: String? = null,
    onEmptyAction: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "state_animation"
    ) { currentState ->
        when (currentState) {
            is UiState.Loading -> {
                LoadingState(
                    message = loadingMessage,
                    modifier = modifier
                )
            }
            
            is UiState.Success -> {
                val data = currentState.data
                if (data != null) {
                    content(data)
                } else {
                    EmptyState(
                        message = emptyMessage,
                        subtitle = emptySubtitle,
                        actionText = emptyActionText,
                        onAction = onEmptyAction,
                        modifier = modifier
                    )
                }
            }
            
            is UiState.Error -> {
                ErrorState(
                    message = currentState.message,
                    modifier = modifier,
                    onRetry = onRetry,
                    onBack = onBack
                )
            }
            
            is UiState.Empty -> {
                EmptyState(
                    message = emptyMessage,
                    subtitle = emptySubtitle,
                    actionText = emptyActionText,
                    onAction = onEmptyAction,
                    modifier = modifier
                )
            }
        }
    }
}

/**
 * 通用UI状态封装
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}

/**
 * 通用消息提示组件
 */
@Composable
fun MessageBar(
    message: String,
    type: MessageType = MessageType.Info,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    val colors = when (type) {
        MessageType.Success -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.CheckCircle
        )
        MessageType.Error -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.Error
        )
        MessageType.Warning -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            Icons.Default.Warning
        )
        MessageType.Info -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Info
        )
    }
    val (backgroundColor, contentColor, icon) = colors

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            
            onDismiss?.let { dismiss ->
                IconButton(
                    onClick = dismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * 消息类型枚举
 */
enum class MessageType {
    Success,
    Error,
    Warning,
    Info
}

/**
 * 通用刷新组件
 */
@Composable
fun PullToRefreshContainer(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        content()
        
        if (isRefreshing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "刷新中...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}