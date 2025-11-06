package com.example.kdyaimap.util

import android.content.Context
import androidx.collection.LruCache
import androidx.room.RoomDatabase
import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.core.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class CacheManager(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    
    private val memoryCache = ConcurrentHashMap<String, CacheEntry<*>>()
    private val diskCache = LruCache<String, String>(50)
    
    private val _cacheStats = MutableStateFlow(CacheStats())
    val cacheStats: StateFlow<CacheStats> = _cacheStats.asStateFlow()
    
    data class CacheEntry<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis(),
        val ttl: Long = TimeUnit.MINUTES.toMillis(30),
        val accessCount: Long = 0
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > ttl
        fun incrementAccess(): CacheEntry<T> = copy(accessCount = accessCount + 1)
    }
    
    data class CacheStats(
        val memoryHits: Long = 0,
        val memoryMisses: Long = 0,
        val diskHits: Long = 0,
        val diskMisses: Long = 0,
        val memorySize: Int = 0,
        val diskSize: Int = 0
    ) {
        val memoryHitRate: Double get() = if (memoryHits + memoryMisses > 0) memoryHits.toDouble() / (memoryHits + memoryMisses) else 0.0
        val diskHitRate: Double get() = if (diskHits + diskMisses > 0) diskHits.toDouble() / (diskHits + diskMisses) else 0.0
    }
    
    init {
        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(TimeUnit.MINUTES.toMillis(5))
                cleanupExpiredEntries()
            }
        }
    }
    
    suspend fun <T> get(
        key: String,
        loader: suspend () -> T,
        ttl: Long = TimeUnit.MINUTES.toMillis(30)
    ): T = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                @Suppress("UNCHECKED_CAST")
                memoryCache[key]?.let { entry ->
                    if (!entry.isExpired()) {
                        @Suppress("UNCHECKED_CAST")
                        val incrementedEntry = entry.incrementAccess() as CacheEntry<T>
                        memoryCache[key] = incrementedEntry
                        updateStats { it.copy(memoryHits = it.memoryHits + 1) }
                        @Suppress("UNCHECKED_CAST")
                        return@withContext incrementedEntry.data
                    } else {
                        memoryCache.remove(key)
                    }
                }
                
                diskCache.get(key)?.let { cachedData ->
                    try {
                        val data = deserializeData<T>(cachedData)
                        val entry = CacheEntry(data, ttl = ttl)
                        memoryCache[key] = entry
                        updateStats { it.copy(diskHits = it.diskHits + 1, memorySize = memoryCache.size) }
                        return@withContext data
                    } catch (e: Exception) {
                        diskCache.remove(key)
                    }
                }
                
                updateStats { it.copy(memoryMisses = it.memoryMisses + 1, diskMisses = it.diskMisses + 1) }
                
                val data = loader()
                val entry = CacheEntry(data, ttl = ttl)
                memoryCache[key] = entry
                
                try {
                    diskCache.put(key, serializeData(data))
                } catch (e: Exception) {
                }
                
                updateStats { it.copy(memorySize = memoryCache.size, diskSize = diskCache.size()) }
                
                data
            } catch (e: Exception) {
                loader()
            }
        }
    }
    
    suspend fun <T> put(key: String, data: T, ttl: Long = TimeUnit.MINUTES.toMillis(30)) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val entry = CacheEntry(data, ttl = ttl)
            memoryCache[key] = entry
            
            try {
                diskCache.put(key, serializeData(data))
            } catch (e: Exception) {
            }
            
            updateStats { it.copy(memorySize = memoryCache.size, diskSize = diskCache.size()) }
        }
    }
    
    suspend fun invalidate(key: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            memoryCache.remove(key)
            diskCache.remove(key)
            updateStats { it.copy(memorySize = memoryCache.size, diskSize = diskCache.size()) }
        }
    }
    
    suspend fun invalidatePattern(pattern: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val keysToRemove = memoryCache.keys.filter { it.contains(pattern) }
            keysToRemove.forEach { key ->
                memoryCache.remove(key)
                diskCache.remove(key)
            }
            updateStats { it.copy(memorySize = memoryCache.size, diskSize = diskCache.size()) }
        }
    }
    
    suspend fun clear() = withContext(Dispatchers.IO) {
        mutex.withLock {
            memoryCache.clear()
            diskCache.evictAll()
            updateStats { CacheStats() }
        }
    }
    
    private suspend fun cleanupExpiredEntries() = withContext(Dispatchers.IO) {
        mutex.withLock {
            val expiredKeys = memoryCache.filter { (_, entry) -> entry.isExpired() }.keys
            expiredKeys.forEach { key ->
                memoryCache.remove(key)
                diskCache.remove(key)
            }
            
            if (memoryCache.size > 100) {
                val sortedEntries = memoryCache.entries.sortedByDescending { it.value.accessCount }
                val toKeep = sortedEntries.take(80)
                memoryCache.clear()
                toKeep.forEach { (key, value) -> memoryCache[key] = value }
            }
            
            updateStats { it.copy(memorySize = memoryCache.size, diskSize = diskCache.size()) }
        }
    }
    
    private fun updateStats(update: (CacheStats) -> CacheStats) {
        _cacheStats.value = update(_cacheStats.value)
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun <T> deserializeData(data: String): T {
        return when {
            data.startsWith("CampusEvent:") -> {
                val parts = data.removePrefix("CampusEvent:").split("|")
                CampusEvent(
                    id = parts[0].toLong(),
                    eventType = parts[1],
                    title = parts[2],
                    description = parts[3],
                    authorId = parts[4].toLong(),
                    latitude = parts[5].toDouble(),
                    longitude = parts[6].toDouble(),
                    locationName = parts[7],
                    creationTimestamp = parts[8].toLong(),
                    status = com.example.kdyaimap.core.model.EventStatus.valueOf(parts[9])
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown data type")
        }
    }
    
    private fun <T> serializeData(data: T): String {
        return when (data) {
            is CampusEvent -> {
                "CampusEvent:${data.id}|${data.eventType}|${data.title}|${data.description}|${data.authorId}|${data.latitude}|${data.longitude}|${data.locationName}|${data.creationTimestamp}|${data.status}"
            }
            else -> throw IllegalArgumentException("Unsupported data type")
        }
    }
    
    fun getCacheInfo(): Map<String, Any> {
        return mapOf(
            "memoryCacheSize" to memoryCache.size,
            "diskCacheSize" to diskCache.size(),
            "memoryHitRate" to _cacheStats.value.memoryHitRate,
            "diskHitRate" to _cacheStats.value.diskHitRate,
            "totalHits" to (_cacheStats.value.memoryHits + _cacheStats.value.diskHits),
            "totalMisses" to (_cacheStats.value.memoryMisses + _cacheStats.value.diskMisses)
        )
    }
}

object CacheKeys {
    const val ALL_EVENTS = "all_events"
    const val EVENTS_BY_TYPE = "events_by_type"
    const val EVENTS_BY_STATUS = "events_by_status"
    const val USER_PROFILE = "user_profile"
    const val USER_HISTORY = "user_history"
    
    fun eventsByType(eventType: String): String = "$EVENTS_BY_TYPE:$eventType"
    fun eventsByStatus(status: String): String = "$EVENTS_BY_STATUS:$status"
    fun userProfile(userId: Long): String = "$USER_PROFILE:$userId"
    fun userHistory(userId: Long): String = "$USER_HISTORY:$userId"
}