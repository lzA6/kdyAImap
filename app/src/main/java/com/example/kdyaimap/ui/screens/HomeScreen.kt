package com.example.kdyaimap.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kdyaimap.R
import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.core.model.EventStatus
import com.example.kdyaimap.core.model.EventType
import com.example.kdyaimap.ui.viewmodel.HomeViewModel
import com.example.kdyaimap.ui.viewmodel.HomeState
import java.util.Calendar

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    onEventClick: (Long) -> Unit
) {
    val homeState by homeViewModel.homeState.collectAsState()
    var showFilters by remember { mutableStateOf(false) }
    val isRefreshing by homeViewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullRefreshState(isRefreshing, { homeViewModel.refresh() })

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CategoryTabs(onCategorySelected = { category ->
                homeViewModel.selectCategory(category)
            })
            IconButton(onClick = { showFilters = !showFilters }) {
                Icon(Icons.Default.FilterList, contentDescription = "筛选")
            }
        }

        AnimatedVisibility(
            visible = showFilters,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            FilterPanel(
                onStatusSelected = { homeViewModel.selectStatus(it) },
                onDateRangeSelected = { homeViewModel.selectDateRange(it) }
            )
        }

        Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
            when (val state = homeState) {
                is HomeState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is HomeState.Success -> {
                    EventList(events = state.events, onEventClick = onEventClick)
                }
                is HomeState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun CategoryTabs(onCategorySelected: (EventType?) -> Unit) {
    val categories = remember { listOf(null) + EventType.values() }
    var selectedCategory by remember { mutableStateOf<EventType?>(null) }
    val selectedIndex = remember(selectedCategory) { categories.indexOf(selectedCategory) }

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        edgePadding = 0.dp
    ) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory
            val categoryName = remember(category) {
                when (category) {
                    null -> "全部"
                    com.example.kdyaimap.core.model.EventType.TRADE -> "二手交易"
                    com.example.kdyaimap.core.model.EventType.SOCIAL -> "社交邀约"
                    com.example.kdyaimap.core.model.EventType.HELP -> "生活互助"
                    com.example.kdyaimap.core.model.EventType.STUDY -> "学习交流"
                    com.example.kdyaimap.core.model.EventType.CLUB_ACTIVITY -> "社团活动"
                    com.example.kdyaimap.core.model.EventType.ACADEMIC -> "学术交流"
                    com.example.kdyaimap.core.model.EventType.SPORTS -> "体育运动"
                    com.example.kdyaimap.core.model.EventType.ENTERTAINMENT -> "娱乐活动"
                    com.example.kdyaimap.core.model.EventType.VOLUNTEER -> "志愿服务"
                    com.example.kdyaimap.core.model.EventType.OTHER -> "其他"
                    com.example.kdyaimap.core.model.EventType.FOOD -> "餐饮美食"
                    com.example.kdyaimap.core.model.EventType.SERVICE -> "便民服务"
                    com.example.kdyaimap.core.model.EventType.DORMITORY -> "生活住宿"
                }
            }
            
            Tab(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        selectedCategory = category
                        onCategorySelected(category)
                    }
                },
                text = {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventList(events: List<CampusEvent>, onEventClick: (Long) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        state = rememberLazyListState()
    ) {
        items(
            count = events.size,
            key = { index -> events[index].id }
        ) { index ->
            val event = events[index]
            EventCard(
                event = event,
                onClick = { onEventClick(event.id) },
                modifier = Modifier.animateItem(
                    fadeInSpec = tween(durationMillis = 250),
                    fadeOutSpec = tween(durationMillis = 250)
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCard(
    event: CampusEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val eventTitle = remember(event.title) { event.title }
    val eventDescription = remember(event.description) {
        event.description.takeIf { it.length <= 100 } ?: "${event.description.take(97)}..."
    }
    val eventTypeName = remember(event.eventType) {
        when (event.eventType) {
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
    val locationName = remember(event.locationName) { event.locationName }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = eventTitle,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = eventDescription,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = eventTypeName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Text(
                    text = locationName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPanel(
    onStatusSelected: (EventStatus?) -> Unit,
    onDateRangeSelected: (Pair<Long, Long>?) -> Unit
) {
    var selectedStatus by remember { mutableStateOf<EventStatus?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("筛选条件", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Status Filter
        var statusExpanded by remember { mutableStateOf(false) }
        val statuses = listOf(null) + EventStatus.values()
        ExposedDropdownMenuBox(
            expanded = statusExpanded,
            onExpandedChange = { statusExpanded = !statusExpanded }
        ) {
            OutlinedTextField(
                value = when (selectedStatus) {
                    null -> "所有状态"
                    com.example.kdyaimap.core.model.EventStatus.PENDING_REVIEW -> "待审核"
                    com.example.kdyaimap.core.model.EventStatus.APPROVED -> "已通过"
                    com.example.kdyaimap.core.model.EventStatus.REJECTED -> "已拒绝"
                    com.example.kdyaimap.core.model.EventStatus.CLOSED -> "已关闭"
                    com.example.kdyaimap.core.model.EventStatus.CANCELLED -> "已取消"
                },
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                modifier = Modifier.menuAnchor(
                    MenuAnchorType.PrimaryNotEditable,
                    true
                )
            )
            ExposedDropdownMenu(
                expanded = statusExpanded,
                onDismissRequest = { statusExpanded = false }
            ) {
                statuses.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(
                            when (status) {
                                null -> "所有状态"
                                com.example.kdyaimap.core.model.EventStatus.PENDING_REVIEW -> "待审核"
                                com.example.kdyaimap.core.model.EventStatus.APPROVED -> "已通过"
                                com.example.kdyaimap.core.model.EventStatus.REJECTED -> "已拒绝"
                                com.example.kdyaimap.core.model.EventStatus.CLOSED -> "已关闭"
                                com.example.kdyaimap.core.model.EventStatus.CANCELLED -> "已取消"
                            }
                        ) },
                        onClick = {
                            selectedStatus = status
                            onStatusSelected(status)
                            statusExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date Range Filter
        Button(onClick = { showDatePicker = true }) {
            Text("选择日期范围")
        }

        if (showDatePicker) {
            val dateRangePickerState = rememberDateRangePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDatePicker = false
                            val start = dateRangePickerState.selectedStartDateMillis
                            val end = dateRangePickerState.selectedEndDateMillis
                            if (start != null && end != null) {
                                onDateRangeSelected(Pair(start, end))
                            }
                        }
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDatePicker = false
                            onDateRangeSelected(null)
                        }
                    ) {
                        Text("清除")
                    }
                }
            ) {
                DateRangePicker(state = dateRangePickerState)
            }
        }
    }
}