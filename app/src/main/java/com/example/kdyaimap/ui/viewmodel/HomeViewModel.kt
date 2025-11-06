package com.example.kdyaimap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.core.model.EventStatus
import com.example.kdyaimap.core.model.EventType
import com.example.kdyaimap.domain.repository.CampusEventRepository
import com.example.kdyaimap.util.NetworkStatusManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val campusEventRepository: CampusEventRepository,
    private val networkStatusManager: NetworkStatusManager
) : ViewModel() {

    private val _homeState = MutableStateFlow<HomeState>(HomeState.Loading)
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    private val _selectedCategory = MutableStateFlow<EventType?>(null)
    private val _selectedStatus = MutableStateFlow<EventStatus?>(null)
    private val _selectedDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    private val _selectedDistance = MutableStateFlow<Float?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)
    
    private val cache = mutableMapOf<String, List<CampusEvent>>()
    private val cacheMutex = Mutex()
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            // 监听各个Flow的变化
            launch {
                _selectedCategory.collect { triggerRefresh() }
            }
            launch {
                _selectedStatus.collect { triggerRefresh() }
            }
            launch {
                _selectedDateRange.collect { triggerRefresh() }
            }
            launch {
                _selectedDistance.collect { triggerRefresh() }
            }
            launch {
                _refreshTrigger.collect { triggerRefresh() }
            }
            launch {
                networkStatusManager.isNetworkAvailable.collect { triggerRefresh() }
            }
        }
        
        // 初始加载
        triggerRefresh()
    }

    private fun triggerRefresh() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // 防抖
            
            val category = _selectedCategory.value
            val status = _selectedStatus.value
            val dateRange = _selectedDateRange.value
            val distance = _selectedDistance.value
            val isNetworkAvailable = networkStatusManager.isNetworkAvailable.value
            
            _homeState.value = HomeState.Loading
            
            if (!isNetworkAvailable) {
                _homeState.value = HomeState.Error("网络连接不可用，请检查网络设置")
                return@launch
            }
            
            try {
                val cacheKey = buildCacheKey(category, status, dateRange)
                
                cacheMutex.withLock {
                    cache[cacheKey]?.let { cachedEvents ->
                        _homeState.value = HomeState.Success(cachedEvents)
                        return@launch
                    }
                }
                
                // 从网络获取所有已批准的活动
                val result = campusEventRepository.getApprovedEventsNetwork(null)
                
                result.fold(
                    onSuccess = { allEvents ->
                        // 在内存中进行筛选
                        var filteredEvents = allEvents
                        if (category != null) {
                            filteredEvents = filteredEvents.filter { it.eventType == category.name }
                        }
                        if (status != null) {
                            filteredEvents = filteredEvents.filter { it.status == status }
                        }
                        // 日期范围筛选
                        dateRange?.let { range ->
                            filteredEvents = filteredEvents.filter { event ->
                                event.creationTimestamp >= range.first && event.creationTimestamp <= range.second
                            }
                        }
                        
                        cacheMutex.withLock {
                            cache[cacheKey] = filteredEvents
                            if (cache.size > 20) {
                                val oldestKey = cache.keys.first()
                                cache.remove(oldestKey)
                            }
                        }
                        
                        _homeState.value = HomeState.Success(filteredEvents)
                    },
                    onFailure = { exception ->
                        _homeState.value = HomeState.Error(exception.message ?: "加载失败")
                    }
                )
            } catch (e: Exception) {
                _homeState.value = HomeState.Error(e.message ?: "未知错误")
            }
        }
    }

    private fun buildCacheKey(
        category: EventType?,
        status: EventStatus?,
        dateRange: Pair<Long, Long>?
    ): String {
        return "${category?.name ?: "all"}_${status?.name ?: "all"}_${dateRange?.first ?: "nostart"}_${dateRange?.second ?: "noend"}"
    }

    fun selectCategory(category: EventType?) {
        _selectedCategory.value = category
    }

    fun selectStatus(status: EventStatus?) {
        _selectedStatus.value = status
    }

    fun selectDateRange(range: Pair<Long, Long>?) {
        _selectedDateRange.value = range
    }

    fun selectDistance(distance: Float?) {
        _selectedDistance.value = distance
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            cacheMutex.withLock {
                cache.clear()
            }
            _refreshTrigger.value = _refreshTrigger.value + 1
            delay(500)
            _isRefreshing.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}

sealed class HomeState {
    object Loading : HomeState()
    data class Success(val events: List<CampusEvent>) : HomeState()
    data class Error(val message: String) : HomeState()
}