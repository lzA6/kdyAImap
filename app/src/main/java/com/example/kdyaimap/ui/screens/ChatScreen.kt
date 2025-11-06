package com.example.kdyaimap.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kdyaimap.core.model.MessageType
import com.example.kdyaimap.ui.viewmodel.PrivateMessageViewModel
import com.example.kdyaimap.ui.viewmodel.MessageState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: Long,
    participantName: String,
    onBack: () -> Unit,
    onAddFriend: () -> Unit,
    privateMessageViewModel: PrivateMessageViewModel = hiltViewModel()
) {
    val messageState by privateMessageViewModel.messageState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    var messageText by remember { mutableStateOf("") }
    
    // 加载消息
    LaunchedEffect(conversationId) {
        privateMessageViewModel.loadMessages(conversationId)
    }
    
    // 自动滚动到底部
    LaunchedEffect(messageState) {
        val state = messageState
        if (state is MessageState.Success) {
            coroutineScope.launch {
                listState.animateScrollToItem(state.messages.size - 1)
            }
        }
    }
    
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
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = participantName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 加好友按钮
            IconButton(onClick = onAddFriend) {
                Icon(Icons.Default.PersonAdd, contentDescription = "添加好友")
            }
        }
        
        // 消息列表
        when (val state = messageState) {
            is MessageState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is MessageState.Success -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.messages) { message ->
                        MessageItem(
                            message = message,
                            isFromMe = message.senderId == 1L // TODO: 从用户状态获取当前用户ID
                        )
                    }
                }
            }
            is MessageState.Error -> {
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
                        Button(onClick = { privateMessageViewModel.loadMessages(conversationId) }) {
                            Text("重试")
                        }
                    }
                }
            }
            else -> {}
        }
        
        // 输入框
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 图片按钮
            IconButton(
                onClick = { /* TODO: 选择图片 */ },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = "发送图片",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 文本输入框
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("输入消息...") },
                maxLines = 3,
                shape = RoundedCornerShape(24.dp)
            )
            
            // 发送按钮
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        privateMessageViewModel.sendMessage(
                            conversationId = conversationId,
                            senderId = 1L, // TODO: 从用户状态获取
                            senderName = "我", // TODO: 从用户状态获取
                            receiverId = 2L, // TODO: 从会话信息获取
                            receiverName = participantName,
                            content = messageText.trim()
                        )
                        messageText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (messageText.isNotBlank()) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "发送",
                    tint = if (messageText.isNotBlank()) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MessageItem(
    message: com.example.kdyaimap.core.model.PrivateMessage,
    isFromMe: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
        ) {
            // 消息气泡
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = if (isFromMe) 16.dp else 4.dp,
                            topEnd = if (isFromMe) 4.dp else 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .background(
                        if (isFromMe) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(12.dp)
            ) {
                when (message.messageType) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isFromMe) 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    MessageType.IMAGE -> {
                        // TODO: 显示图片
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = "图片",
                                modifier = Modifier.size(48.dp),
                                tint = if (isFromMe) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "[图片]",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isFromMe) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    MessageType.VOICE -> {
                        // TODO: 显示语音
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "语音",
                                modifier = Modifier.size(20.dp),
                                tint = if (isFromMe) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "[语音]",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isFromMe) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    MessageType.SYSTEM -> {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 时间和状态
            Row(
                horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatMessageTime(message.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (isFromMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.DoneAll,
                        contentDescription = "已读",
                        modifier = Modifier.size(12.dp),
                        tint = if (message.isRead) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatMessageTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "刚刚"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
        diff < 24 * 60 * 60 * 1000 -> {
            val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
        else -> {
            val sdf = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
    }
}