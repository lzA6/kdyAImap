package com.example.kdyaimap.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.core.model.EventType
import com.example.kdyaimap.ui.viewmodel.UserEventsViewModel
import com.example.kdyaimap.ui.viewmodel.UserEventsState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEventsScreen(
    userEventsViewModel: UserEventsViewModel = hiltViewModel(),
    onNavigateToEventDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val userEventsState by userEventsViewModel.userEventsState.collectAsState()
    
    LaunchedEffect(Unit) {
        userEventsViewModel.loadUserEvents()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的活动") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (val state = userEventsState) {
                is UserEventsState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is UserEventsState.Success -> {
                    // 发布的活动
                    if (state.publishedEvents.isNotEmpty()) {
                        SectionHeader(
                            title = "我发布的",
                            count = state.publishedEvents.size,
                            icon = Icons.Default.Publish
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.publishedEvents) { event ->
                                EventCard(
                                    event = event,
                                    onClick = { onNavigateToEventDetail(event.id.toString()) }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 参与的活动
                    if (state.participatedEvents.isNotEmpty()) {
                        SectionHeader(
                            title = "我参与的",
                            count = state.participatedEvents.size,
                            icon = Icons.Default.Group
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.participatedEvents) { event ->
                                EventCard(
                                    event = event,
                                    onClick = { onNavigateToEventDetail(event.id.toString()) }
                                )
                            }
                        }
                    }
                    
                    // 如果没有任何活动
                    if (state.publishedEvents.isEmpty() && state.participatedEvents.isEmpty()) {
                        EmptyState(
                            title = "暂无活动",
                            description = "你还没有发布或参与任何活动",
                            icon = Icons.Default.Event
                        )
                    }
                }
                
                is UserEventsState.Error -> {
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
                            Button(onClick = { userEventsViewModel.loadUserEvents() }) {
                                Text("重试")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "($count)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EventCard(
    event: CampusEvent,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                
                // 活动类型标签
                EventTypeChip(eventType = event.eventType)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = dateFormat.format(Date(event.startTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = event.locationName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 参与人数和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.currentParticipants}/${event.maxParticipants}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // 活动状态
                EventStatusChip(status = event.status.name)
            }
        }
    }
}

@Composable
private fun EventTypeChip(eventType: String) {
    val (label, color) = when (eventType) {
        "TRADE" -> "二手" to MaterialTheme.colorScheme.primary
        "SOCIAL" -> "社交" to MaterialTheme.colorScheme.secondary
        "HELP" -> "互助" to MaterialTheme.colorScheme.tertiary
        "STUDY" -> "学习" to MaterialTheme.colorScheme.primary
        "CLUB_ACTIVITY" -> "社团" to MaterialTheme.colorScheme.secondary
        "ACADEMIC" -> "学术" to MaterialTheme.colorScheme.tertiary
        "SPORTS" -> "运动" to MaterialTheme.colorScheme.primary
        "ENTERTAINMENT" -> "娱乐" to MaterialTheme.colorScheme.error
        "VOLUNTEER" -> "志愿" to MaterialTheme.colorScheme.secondary
        "OTHER" -> "其他" to MaterialTheme.colorScheme.outline
        "FOOD" -> "餐饮" to MaterialTheme.colorScheme.primary
        "SERVICE" -> "服务" to MaterialTheme.colorScheme.secondary
        "DORMITORY" -> "住宿" to MaterialTheme.colorScheme.tertiary
        else -> "其他" to MaterialTheme.colorScheme.outline
    }
    
    Surface(
        modifier = Modifier,
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun EventStatusChip(status: String) {
    val (label, color) = when (status) {
        "PENDING" -> "待审核" to MaterialTheme.colorScheme.outline
        "APPROVED" -> "已通过" to MaterialTheme.colorScheme.primary
        "REJECTED" -> "已拒绝" to MaterialTheme.colorScheme.error
        "CANCELLED" -> "已取消" to MaterialTheme.colorScheme.onSurfaceVariant
        else -> status to MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        modifier = Modifier,
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun EmptyState(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}