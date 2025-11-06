package com.example.kdyaimap.ui.screens

import androidx.compose.foundation.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kdyaimap.core.model.EventType
import com.example.kdyaimap.core.model.EventStatus
import com.example.kdyaimap.ui.viewmodel.EventViewModel
import com.example.kdyaimap.ui.viewmodel.EventState
import com.example.kdyaimap.ui.viewmodel.OrganizerState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: Long,
    eventViewModel: EventViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onEdit: (String) -> Unit = {},
    onNavigateToMap: (Double, Double, String) -> Unit = { _, _, _ -> },
    onContactOrganizer: (String) -> Unit = {}
) {
    val context = LocalContext.current
    
    LaunchedEffect(eventId) {
        eventViewModel.loadEventNetwork(eventId.toString())
    }
    
    val eventState by eventViewModel.eventState.collectAsState()
    val organizerState by eventViewModel.organizerState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // 当活动加载成功后，加载组织者信息
    LaunchedEffect(eventState) {
        val state = eventState
        if (state is EventState.Success && state.event != null) {
            eventViewModel.loadOrganizerNetwork(state.event!!.authorId.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("活动详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    val state = eventState
                    if (state is EventState.Success) {
                        state.event?.let { event ->
                            // 编辑按钮（仅组织者或管理员可见）
                            IconButton(onClick = { onEdit(event.id.toString()) }) {
                                Icon(Icons.Default.Edit, contentDescription = "编辑")
                            }
                            // 删除按钮（仅组织者或管理员可见）
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除")
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (val state = eventState) {
            is EventState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("加载中...")
                    }
                }
            }
            
            is EventState.Success -> {
                val event = state.event
                if (event != null) {
                    EventDetailContent(
                        event = event,
                        modifier = Modifier.fillMaxSize().padding(padding),
                        onNavigateToMap = onNavigateToMap,
                        onContactOrganizer = onContactOrganizer,
                        organizerState = organizerState
                    )
                } else {
                    EmptyState(modifier = Modifier.fillMaxSize().padding(padding))
                }
            }
            
            is EventState.Error -> {
                ErrorState(
                    message = state.message,
                    modifier = Modifier.fillMaxSize().padding(padding),
                    onRetry = { eventViewModel.loadEventNetwork(eventId.toString()) },
                    onBack = onBack
                )
            }
            
            is EventState.Idle -> {}
            is EventState.ImageUploadSuccess -> {}
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这个活动吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        eventViewModel.deleteEventNetwork(eventId.toString())
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
private fun EventDetailContent(
    event: com.example.kdyaimap.core.model.CampusEvent,
    modifier: Modifier = Modifier,
    onNavigateToMap: (Double, Double, String) -> Unit,
    onContactOrganizer: (String) -> Unit,
    organizerState: OrganizerState
) {
    val context = LocalContext.current
    
    Column(
        modifier = modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 活动标题和状态
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            StatusChip(status = event.status)
        }
        
        // 活动图片
        val images = event.images
        if (!images.isNullOrEmpty()) {
            EventImageCarousel(images = images)
        }
        
        // 组织者信息卡片
        OrganizerInfoCard(organizerState = organizerState)
        
        // 活动信息卡片
        EventInfoCard(event = event)
        
        // 活动描述
        if (event.description.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "活动描述",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        // 参与者信息
        val maxParticipants = event.maxParticipants
        if (maxParticipants != null && maxParticipants > 0) {
            ParticipantsInfoCard(
                currentParticipants = event.currentParticipants,
                maxParticipants = maxParticipants
            )
        }
        
        // 活动要求
        val requirements = event.requirements
        if (!requirements.isNullOrBlank()) {
            RequirementsCard(requirements = requirements)
        }
        
        // 联系信息
        val contactInfo = event.contactInfo
        if (!contactInfo.isNullOrBlank()) {
            ContactInfoCard(contactInfo = contactInfo)
        }
        
        // 操作按钮
        ActionButtons(
            event = event,
            onNavigateToMap = onNavigateToMap,
            onContactOrganizer = onContactOrganizer
        )
    }
}

@Composable
private fun StatusChip(status: EventStatus) {
    val (text, color) = when (status) {
        EventStatus.APPROVED -> "已审核" to MaterialTheme.colorScheme.primary
        EventStatus.PENDING_REVIEW -> "待审核" to MaterialTheme.colorScheme.secondary
        EventStatus.REJECTED -> "已拒绝" to MaterialTheme.colorScheme.error
        EventStatus.CLOSED -> "已关闭" to MaterialTheme.colorScheme.outline
        EventStatus.CANCELLED -> "已取消" to MaterialTheme.colorScheme.outline
    }
    
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EventImageCarousel(images: List<String>) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(images.first())
                .crossfade(true)
                .build(),
            contentDescription = "活动图片",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // 图片指示器
        if (images.size > 1) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "1/${images.size}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun EventInfoCard(event: com.example.kdyaimap.core.model.CampusEvent) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 活动类型
            InfoRow(
                icon = Icons.Default.Category,
                label = "活动类型",
                value = getEventTypeName(event.eventType)
            )
            
            // 活动时间
            if (event.startTime > 0) {
                InfoRow(
                    icon = Icons.Default.Schedule,
                    label = "活动时间",
                    value = formatTimestamp(event.startTime)
                )
                
                if (event.endTime > 0 && event.endTime != event.startTime) {
                    InfoRow(
                        icon = Icons.Default.Schedule,
                        label = "结束时间",
                        value = formatTimestamp(event.endTime)
                    )
                }
            }
            
            // 活动地点
            InfoRow(
                icon = Icons.Default.LocationOn,
                label = "活动地点",
                value = event.locationName,
                isClickable = true
            )
            
            // 最大参与人数
            val maxParticipants = event.maxParticipants
            if (maxParticipants != null && maxParticipants > 0) {
                InfoRow(
                    icon = Icons.Default.Group,
                    label = "最大参与人数",
                    value = "${maxParticipants}人"
                )
            }
            
            // 创建时间
            if (event.creationTimestamp > 0) {
                InfoRow(
                    icon = Icons.Default.AccessTime,
                    label = "发布时间",
                    value = formatTimestamp(event.creationTimestamp)
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isClickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.then(
                    if (isClickable) {
                        Modifier.clickable(onClick = onClick)
                    } else Modifier
                ),
                textDecoration = if (isClickable) TextDecoration.Underline else null,
                color = if (isClickable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ActionButtons(
    event: com.example.kdyaimap.core.model.CampusEvent,
    onNavigateToMap: (Double, Double, String) -> Unit,
    onContactOrganizer: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 导航到活动地点
        Button(
            onClick = { onNavigateToMap(event.latitude, event.longitude, event.locationName) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Navigation, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("导航到活动地点")
        }
        
        // 联系组织者
        OutlinedButton(
            onClick = { onContactOrganizer(event.authorId.toString()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ContactPhone, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("联系组织者")
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Event,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "活动不存在",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = modifier,
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
                text = "加载失败",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onRetry) {
                    Text("重试")
                }
                OutlinedButton(onClick = onBack) {
                    Text("返回")
                }
            }
        }
    }
}

@Composable
private fun OrganizerInfoCard(organizerState: OrganizerState) {
    when (organizerState) {
        is OrganizerState.Loading -> {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 2.dp
                    )
                    Text("加载组织者信息...")
                }
            }
        }
        
        is OrganizerState.Success -> {
            val organizer = organizerState.organizer
            if (organizer != null) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 组织者头像
                        if (organizer.avatar.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(organizer.avatar)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "组织者头像",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(24.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = organizer.username.firstOrNull()?.uppercase()?.toString() ?: "?",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        // 组织者信息
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = organizer.username,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            if (organizer.bio.isNotBlank()) {
                                Text(
                                    text = organizer.bio,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                        
                        // 组织者角色标签
                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "组织者",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
        
        is OrganizerState.Error -> {
            // 静默处理错误，不显示错误信息
        }
        
        is OrganizerState.Idle -> {
            // 静默处理空闲状态
        }
    }
}

@Composable
private fun ParticipantsInfoCard(
    currentParticipants: Int,
    maxParticipants: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "参与情况",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "当前参与人数",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "$currentParticipants / $maxParticipants",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (currentParticipants >= maxParticipants) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
            
            // 进度条
            LinearProgressIndicator(
                progress = { (currentParticipants.toFloat() / maxParticipants.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (currentParticipants >= maxParticipants) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            
            if (currentParticipants >= maxParticipants) {
                Text(
                    text = "参与人数已满",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun RequirementsCard(requirements: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "参与要求",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = requirements,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ContactInfoCard(contactInfo: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "联系方式",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = contactInfo,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

fun getEventTypeName(eventType: String): String {
    return when (eventType) {
        "TRADE" -> "二手交易"
        "SOCIAL" -> "社交邀约"
        "HELP" -> "生活互助"
        "STUDY" -> "学习交流"
        "CLUB_ACTIVITY" -> "社团活动"
        "ACADEMIC" -> "学术交流"
        "SPORTS" -> "体育运动"
        "ENTERTAINMENT" -> "娱乐活动"
        "VOLUNTEER" -> "志愿服务"
        "OTHER" -> "其他"
        "FOOD" -> "餐饮美食"
        "SERVICE" -> "便民服务"
        "DORMITORY" -> "生活住宿"
        else -> "其他"
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "时间格式错误"
    }
}