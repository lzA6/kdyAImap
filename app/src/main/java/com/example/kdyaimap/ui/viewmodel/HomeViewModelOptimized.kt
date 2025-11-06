package com.example.kdyaimap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.core.model.EventStatus
import com.example.kdyaimap.core.model.EventType
import com.example.kdyaimap.domain.usecase.GetFilteredEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class HomeViewModelOptimized @Inject constructor(
    private val getFilteredEventsUseCase: GetFilteredEventsUseCase
) : ViewModel() {

    private val _homeState = MutableStateFlow<HomeStateOptimized>(HomeStateOptimized.Loading)
    val homeState: StateFlow<HomeStateOptimized> = _homeState.asStateFlow()

    private val _selectedCategory = MutableStateFlow<EventType?>(null)
    private val _selectedStatus = MutableStateFlow<EventStatus?>(null)
    private val _selectedDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    private val _selectedDistance = MutableStateFlow<Float?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // 优化：使用 Channel 替代 StateFlow 进行刷新触发
    private val _refreshChannel = Channel<Unit>(Channel.CONFLATED)
    
    // 优化：改进缓存策略，使用 LRU 缓存
    private val eventCache = mutableMapOf<String, CacheEntry>()
    private val cacheMutex = Mutex()
    private val maxCacheSize = 50
    
    // 优化：使用专门的协程作用域
    private val searchScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // 优化：防抖时间配置
    private val debounceTime = 200L
    
    // 优化：缓存条目数据类
    private data class CacheEntry(
        val events: List<CampusEvent>,
        val timestamp: Long,
        val ttl: Long = 5 * 60 * 1000 // 5分钟TTL
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > ttl
    }

    init {
        observeFilterChanges()
    }

    private fun observeFilterChanges() {
        viewModelScope.launch {
            combine(
                _selectedCategory.debounce(debounceTime),
                _selectedStatus.debounce(debounceTime),
                _selectedDateRange.debounce(debounceTime),
                _selectedDistance.debounce(debounceTime),
                _refreshChannel.receiveAsFlow()
            ) { category, status, dateRange, distance, _ ->
                FilterParams(category, status, dateRange, distance)
            }
            .distinctUntilChanged()
            .flatMapLatest { params ->
                loadEventsWithCache(params)
            }
            .catch { e ->
                _homeState.value = HomeStateOptimized.Error(e.message ?: "加载失败")
            }
            .collect { state ->
                _homeState.value = state
            }
        }
    }

    private fun loadEventsWithCache(params: FilterParams): Flow<HomeStateOptimized> = flow {
        emit(HomeStateOptimized.Loading)
        
        try {
            val cacheKey = buildCacheKey(params)
            
            // 优化：检查本地缓存
            cacheMutex.withLock {
                eventCache[cacheKey]?.let { entry ->
                    if (!entry.isExpired()) {
                        emit(HomeStateOptimized.Success(entry.events))
                        return@flow
                    } else {
                        eventCache.remove(cacheKey)
                    }
                }
            }
            
            // 优化：使用 withContext 切换线程
            val events = withContext(Dispatchers.IO) {
                getFilteredEventsUseCase(
                    eventType = params.category,
                    eventStatus = params.status,
                    startTime = params.dateRange?.first,
                    endTime = params.dateRange?.second
                ).first()
            }
            
            // 优化：更新本地缓存
            cacheMutex.withLock {
                eventCache[cacheKey] = CacheEntry(events, System.currentTimeMillis())
                cleanupExpiredCache()
            }
            
            emit(HomeStateOptimized.Success(events))
            
        } catch (e: Exception) {
            emit(HomeStateOptimized.Error(e.message ?: "未知错误"))
        }
    }

    private fun cleanupExpiredCache() {
        val currentTime = System.currentTimeMillis()
        val expiredKeys = eventCache.filter { (_, entry) ->
            entry.isExpired()
        }.keys
        
        expiredKeys.forEach { key ->
            eventCache.remove(key)
        }
        
        // 优化：如果缓存仍然过大，移除最旧的条目
        if (eventCache.size > maxCacheSize) {
            val sortedEntries = eventCache.entries.sortedBy { it.value.timestamp }
            val entriesToRemove = sortedEntries.take(eventCache.size - maxCacheSize)
            entriesToRemove.forEach { (key, _) ->
                eventCache.remove(key)
            }
        }
    }

    private fun buildCacheKey(params: FilterParams): String {
        return "${params.category?.name ?: "all"}_" +
               "${params.status?.name ?: "all"}_" +
               "${params.dateRange?.first ?: "nostart"}_" +
               "${params.dateRange?.second ?: "noend"}_" +
               "${params.distance ?: "nodist"}"
    }

    // 优化：使用数据类封装过滤参数
    private data class FilterParams(
        val category: EventType?,
        val status: EventStatus?,
        val dateRange: Pair<Long, Long>?,
        val distance: Float?
    )

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
            
            // 优化：清空本地缓存
            searchScope.launch {
                cacheMutex.withLock {
                    eventCache.clear()
                }
            }
            
            // 优化：使用 Channel 发送刷新信号
            _refreshChannel.trySend(Unit)
            
            // 优化：减少刷新延迟
            delay(300)
            _isRefreshing.value = false
        }
    }

    // 优化：添加预加载功能
    fun preloadEvents() {
        viewModelScope.launch {
            try {
                val params = FilterParams(null, null, null, null)
                loadEventsWithCache(params).collect { state ->
                    if (state is HomeStateOptimized.Success) {
                        // 预加载完成
                    }
                }
            } catch (e: Exception) {
                // 预加载失败不影响主流程
            }
        }
    }

    // 优化：添加内存压力处理
    fun onLowMemory() {
        viewModelScope.launch {
            cacheMutex.withLock {
                // 在内存不足时清空一半缓存
                val entriesToRemove = eventCache.keys.take(eventCache.size / 2)
                entriesToRemove.forEach { key ->
                    eventCache.remove(key)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        
        // 优化：取消协程作用域
        searchScope.cancel()
        _refreshChannel.close()
        
        // 优化：清理本地缓存
        viewModelScope.launch {
            cacheMutex.withLock {
                eventCache.clear()
            }
        }
    }
}

sealed class HomeStateOptimized {
    object Loading : HomeStateOptimized()
    data class Success(val events: List<CampusEvent>) : HomeStateOptimized()
    data class Error(val message: String) : HomeStateOptimized()
}