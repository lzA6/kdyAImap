package com.example.kdyaimap.util

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

object MemoryLeakProtector {
    private const val TAG = "MemoryLeakProtector"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val trackedReferences = ConcurrentHashMap<String, WeakReference<Any>>()
    private val _memoryPressure = MutableStateFlow(MemoryPressure.NORMAL)
    val memoryPressure: StateFlow<MemoryPressure> = _memoryPressure.asStateFlow()
    
    enum class MemoryPressure {
        NORMAL, WARNING, CRITICAL
    }
    
    data class MemoryStats(
        val totalMemory: Long = 0,
        val usedMemory: Long = 0,
        val freeMemory: Long = 0,
        val maxMemory: Long = 0,
        val memoryUsagePercent: Double = 0.0,
        val trackedObjects: Int = 0
    )
    
    private val _memoryStats = MutableStateFlow(MemoryStats())
    val memoryStats: StateFlow<MemoryStats> = _memoryStats.asStateFlow()
    
    fun trackObject(key: String, obj: Any) {
        trackedReferences[key] = WeakReference(obj)
        cleanupCollectedReferences()
        
        if (trackedReferences.size > 1000) {
            Log.w(TAG, "跟踪对象数量过多: ${trackedReferences.size}")
            forceCleanup()
        }
    }
    
    fun untrackObject(key: String) {
        trackedReferences.remove(key)
    }
    
    private fun cleanupCollectedReferences() {
        val iterator = trackedReferences.entries.iterator()
        var cleanedCount = 0
        
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.get() == null) {
                iterator.remove()
                cleanedCount++
            }
        }
        
        if (cleanedCount > 0) {
            Log.d(TAG, "清理了 $cleanedCount 个已回收的对象引用")
        }
    }
    
    private fun forceCleanup() {
        System.gc()
        scope.launch {
            delay(100)
            cleanupCollectedReferences()
        }
    }
    
    fun startMemoryMonitoring(context: Context) {
        scope.launch {
            while (isActive) {
                try {
                    updateMemoryStats(context)
                    checkMemoryPressure()
                    cleanupCollectedReferences()
                    
                    delay(30000)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "内存监控异常", e)
                    delay(60000)
                }
            }
        }
    }
    
    private fun updateMemoryStats(context: Context) {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory()
        val memoryUsagePercent = (usedMemory.toDouble() / maxMemory * 100)
        
        val stats = MemoryStats(
            totalMemory = totalMemory,
            usedMemory = usedMemory,
            freeMemory = freeMemory,
            maxMemory = maxMemory,
            memoryUsagePercent = memoryUsagePercent,
            trackedObjects = trackedReferences.size
        )
        
        _memoryStats.value = stats
        
        if (memoryUsagePercent > 85) {
            Log.w(TAG, "高内存使用警告: ${"%.1f".format(memoryUsagePercent)}%")
        }
    }
    
    private fun checkMemoryPressure() {
        val stats = _memoryStats.value
        val pressure = when {
            stats.memoryUsagePercent > 85 -> MemoryPressure.CRITICAL
            stats.memoryUsagePercent > 70 -> MemoryPressure.WARNING
            else -> MemoryPressure.NORMAL
        }
        
        if (_memoryPressure.value != pressure) {
            _memoryPressure.value = pressure
            Log.i(TAG, "内存压力状态变更: $pressure")
            
            if (pressure == MemoryPressure.CRITICAL) {
                handleCriticalMemoryPressure()
            }
        }
    }
    
    private fun handleCriticalMemoryPressure() {
        scope.launch {
            try {
                Log.w(TAG, "处理关键内存压力")
                
                forceCleanup()
                
                trackedReferences.clear()
                
                System.gc()
                delay(200)
                
                // 不需要更新内存统计，因为context为null
                
                Log.i(TAG, "关键内存压力处理完成")
                
            } catch (e: Exception) {
                Log.e(TAG, "处理关键内存压力失败", e)
            }
        }
    }
    
    fun getMemoryReport(): String {
        val stats = _memoryStats.value
        return buildString {
            appendLine("=== 内存使用报告 ===")
            appendLine("总内存: ${stats.totalMemory / 1024 / 1024}MB")
            appendLine("已用内存: ${stats.usedMemory / 1024 / 1024}MB")
            appendLine("可用内存: ${stats.freeMemory / 1024 / 1024}MB")
            appendLine("最大内存: ${stats.maxMemory / 1024 / 1024}MB")
            appendLine("使用率: ${"%.1f".format(stats.memoryUsagePercent)}%")
            appendLine("跟踪对象数: ${stats.trackedObjects}")
            appendLine("内存压力: ${_memoryPressure.value}")
            appendLine("==================")
        }
    }
    
    fun cleanup() {
        trackedReferences.clear()
        forceCleanup()
        Log.i(TAG, "内存泄漏防护器已清理")
    }
}