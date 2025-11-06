package com.example.kdyaimap.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.kdyaimap.util.AdvancedPerformanceMonitor
import com.example.kdyaimap.util.ErrorHandler

@Composable
fun HomeScreenOptimized(
    homeViewModel: HomeViewModel = hiltViewModel(),
    onEventClick: (Long) -> Unit
) {
    AdvancedPerformanceMonitor.measureOperation("home_screen_composition", "ui") {
        // HomeScreen composition
    }
    
    val homeState by homeViewModel.homeState.collectAsState()
    var showFilters by remember { mutableStateOf(false) }
    val isRefreshing by homeViewModel.isRefreshing.collectAsState()
    
    val isFilterVisible by remember {
        derivedStateOf { showFilters }
    }

    AdvancedPerformanceMonitor.measureOperation("home_screen_render", "ui") {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CategoryTabsOptimized(onCategorySelected = { category ->
                    AdvancedPerformanceMonitor.measureOperation("category_selection", "ui") {
                        homeViewModel.selectCategory(category)
                    }
                })
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
            }

            if (isFilterVisible) {
                FilterPanelOptimized(
                    onStatusSelected = { status ->
                        AdvancedPerformanceMonitor.measureOperation("status_filter", "ui") {
                            homeViewModel.selectStatus(status)
                        }
                    },
                    onDateRangeSelected = { dateRange ->
                        AdvancedPerformanceMonitor.measureOperation("date_range_filter", "ui") {
                            homeViewModel.selectDateRange(dateRange)
                        }
                    },
                    modifier = Modifier.graphicsLayer {
                        alpha = if (isFilterVisible) 1f else 0f
                    }
                )
            }

            when (val state = homeState) {
                is HomeState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.graphicsLayer {
                                rotationZ = (System.nanoTime() % 3600) / 10f
                            }
                        )
                    }
                }
                is HomeState.Success -> {
                    EventListOptimized(events = state.events, onEventClick = onEventClick)
                }
                is HomeState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTabsOptimized(onCategorySelected: (EventType?) -> Unit) {
    val categories = remember { listOf(null) + EventType.values() }
    var selectedCategory by remember { mutableStateOf<EventType?>(null) }
    
    val selectedIndex by remember {
        derivedStateOf { categories.indexOf(selectedCategory) }
    }

    AdvancedPerformanceMonitor.measureOperation("category_tabs_render", "ui") {
        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            edgePadding = 0.dp
        ) {
            categories.forEach { category ->
                val isSelected = category == selectedCategory
                val categoryName = remember(category) {
                    category?.name?.replaceFirstChar { it.uppercase() } ?: "全部"
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
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EventListOptimized(events: List<CampusEvent>, onEventClick: (Long) -> Unit) {
    val listState = rememberLazyListState()
    
    AdvancedPerformanceMonitor.measureOperation("event_list_render", "ui") {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = listState,
            userScrollEnabled = true
        ) {
            items(
                count = events.size,
                key = { index -> events[index].id },
                contentType = { "event" }
            ) { index ->
                val event = events[index]
                EventCardOptimized(
                    event = event,
                    onClick = {
                        AdvancedPerformanceMonitor.measureOperation("event_click", "ui") {
                            onEventClick(event.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EventCardOptimized(
    event: CampusEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 优化：预计算所有文本，避免重复计算
    val eventTitle = remember(event.title) { event.title }
    val eventDescription = remember(event.description) {
        if (event.description.length <= 100) {
            event.description
        } else {
            "${event.description.take(97)}..."
        }
    }
    val eventTypeName = remember(event.eventType) {
        try {
            EventType.valueOf(event.eventType).name
        } catch (e: IllegalArgumentException) {
            event.eventType
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
fun FilterPanelOptimized(
    onStatusSelected: (EventStatus?) -> Unit,
    onDateRangeSelected: (Pair<Long, Long>?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatus by remember { mutableStateOf<EventStatus?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(16.dp)) {
        Text("Filters", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Status Filter - 优化：简化下拉菜单
        var statusExpanded by remember { mutableStateOf(false) }
        val statuses = remember { listOf(null) + EventStatus.values() }
        
        ExposedDropdownMenuBox(
            expanded = statusExpanded,
            onExpandedChange = { statusExpanded = !statusExpanded }
        ) {
            OutlinedTextField(
                value = selectedStatus?.name ?: "All Statuses",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                modifier = Modifier
                    .menuAnchor(
                        MenuAnchorType.PrimaryNotEditable,
                        true
                    )
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = statusExpanded,
                onDismissRequest = { statusExpanded = false }
            ) {
                statuses.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status?.name ?: "All Statuses") },
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

        // Date Range Filter - 优化：延迟加载日期选择器
        Button(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select Date Range")
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
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDatePicker = false
                            onDateRangeSelected(null)
                        }
                    ) {
                        Text("Clear")
                    }
                }
            ) {
                DateRangePicker(state = dateRangePickerState)
            }
        }
    }
}