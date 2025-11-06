package com.example.kdyaimap.util

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureTimeMillis

object AdvancedPerformanceMonitor {
    private const val TAG = "AdvancedPerformanceMonitor"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val performanceMetrics = ConcurrentHashMap<String, PerformanceMetric>()
    private val renderMetrics = ConcurrentHashMap<String, RenderMetric>()
    private val memorySnapshots = mutableListOf<MemorySnapshot>()
    
    private val _performanceState = MutableStateFlow(PerformanceState())
    val performanceState: StateFlow<PerformanceState> = _performanceState.asStateFlow()
    
    private val sessionId = AtomicLong(0)
    private var sessionStartTime = System.currentTimeMillis()
    
    data class PerformanceMetric(
        val operation: String,
        val count: Long = 0,
        val totalTime: Long = 0,
        val minTime: Long = Long.MAX_VALUE,
        val maxTime: Long = 0,
        val lastTime: Long = 0,
        val averageTime: Double = 0.0,
        val p95Time: Long = 0,
        val p99Time: Long = 0,
        val errorCount: Long = 0
    ) {
        val successRate: Double get() = if (count > 0) ((count - errorCount).toDouble() / count * 100) else 100.0
    }
    
    data class RenderMetric(
        val screen: String,
        val count: Long = 0,
        val totalTime: Long = 0,
        val averageTime: Double = 0.0,
        val skippedFrames: Long = 0,
        val jankCount: Long = 0
    )
    
    data class MemorySnapshot(
        val timestamp: Long,
        val usedMemory: Long,
        val freeMemory: Long,
        val maxMemory: Long,
        val usagePercent: Double
    )
    
    data class PerformanceState(
        val sessionId: Long = 0,
        val sessionDuration: Long = 0,
        val metrics: Map<String, PerformanceMetric> = emptyMap(),
        val renderMetrics: Map<String, RenderMetric> = emptyMap(),
        val currentMemory: MemorySnapshot? = null,
        val averageMemoryUsage: Double = 0.0,
        val totalOperations: Long = 0,
        val errorRate: Double = 0.0
    )
    
    fun startNewSession() {
        val newSessionId = sessionId.incrementAndGet()
        sessionStartTime = System.currentTimeMillis()
        
        performanceMetrics.clear()
        renderMetrics.clear()
        memorySnapshots.clear()
        
        Log.i(TAG, "开始新的性能监控会话: $newSessionId")
    }
    
    fun recordOperation(
        operation: String,
        duration: Long,
        success: Boolean = true,
        category: String = "general"
    ) {
        val key = "${category}_$operation"
        val current = performanceMetrics.getOrDefault(key, PerformanceMetric(operation))
        
        val newTimes = mutableListOf<Long>()
        newTimes.addAll(getRecentTimes(key))
        newTimes.add(duration)
        newTimes.sort()
        
        val newMetric = current.copy(
            count = current.count + 1,
            totalTime = current.totalTime + duration,
            minTime = minOf(current.minTime, duration),
            maxTime = maxOf(current.maxTime, duration),
            lastTime = duration,
            averageTime = (current.totalTime + duration).toDouble() / (current.count + 1),
            p95Time = newTimes[(newTimes.size * 0.95).toInt().coerceAtMost(newTimes.size - 1)],
            p99Time = newTimes[(newTimes.size * 0.99).toInt().coerceAtMost(newTimes.size - 1)],
            errorCount = if (!success) current.errorCount + 1 else current.errorCount
        )
        
        performanceMetrics[key] = newMetric
        
        if (duration > getThresholdForOperation(operation)) {
            Log.w(TAG, "慢操作检测: $operation 耗时 ${duration}ms (阈值: ${getThresholdForOperation(operation)}ms)")
        }
        
        updatePerformanceState()
    }
    
    fun recordRenderTime(
        screen: String,
        duration: Long,
        skippedFrames: Long = 0,
        jankCount: Long = 0
    ) {
        val current = renderMetrics.getOrDefault(screen, RenderMetric(screen))
        
        val newMetric = current.copy(
            count = current.count + 1,
            totalTime = current.totalTime + duration,
            averageTime = (current.totalTime + duration).toDouble() / (current.count + 1),
            skippedFrames = current.skippedFrames + skippedFrames,
            jankCount = current.jankCount + jankCount
        )
        
        renderMetrics[screen] = newMetric
        
        if (duration > 16) {
            Log.w(TAG, "渲染性能警告: $screen 渲染耗时 ${duration}ms (>16ms)")
        }
        
        updatePerformanceState()
    }
    
    fun captureMemorySnapshot(context: Context? = null) {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val freeMemory = maxMemory - usedMemory
        val usagePercent = (usedMemory.toDouble() / maxMemory * 100)
        
        val snapshot = MemorySnapshot(
            timestamp = System.currentTimeMillis(),
            usedMemory = usedMemory,
            freeMemory = freeMemory,
            maxMemory = maxMemory,
            usagePercent = usagePercent
        )
        
        memorySnapshots.add(snapshot)
        
        if (memorySnapshots.size > 100) {
            memorySnapshots.removeAt(0)
        }
        
        if (usagePercent > 85) {
            Log.w(TAG, "高内存使用警告: ${"%.1f".format(usagePercent)}%")
        }
        
        updatePerformanceState()
    }
    
    private fun getRecentTimes(key: String): List<Long> {
        return emptyList()
    }
    
    private fun getThresholdForOperation(operation: String): Long {
        return when {
            operation.contains("database", ignoreCase = true) -> 1000
            operation.contains("network", ignoreCase = true) -> 5000
            operation.contains("render", ignoreCase = true) -> 16
            else -> 500
        }
    }
    
    private fun updatePerformanceState() {
        val currentSessionId = sessionId.get()
        val sessionDuration = System.currentTimeMillis() - sessionStartTime
        
        val totalOperations = performanceMetrics.values.sumOf { it.count }
        val totalErrors = performanceMetrics.values.sumOf { it.errorCount }
        val errorRate = if (totalOperations > 0) (totalErrors.toDouble() / totalOperations * 100) else 0.0
        
        val averageMemoryUsage = if (memorySnapshots.isNotEmpty()) {
            memorySnapshots.map { it.usagePercent }.average()
        } else {
            0.0
        }
        
        _performanceState.value = PerformanceState(
            sessionId = currentSessionId,
            sessionDuration = sessionDuration,
            metrics = performanceMetrics.toMap(),
            renderMetrics = renderMetrics.toMap(),
            currentMemory = memorySnapshots.lastOrNull(),
            averageMemoryUsage = averageMemoryUsage,
            totalOperations = totalOperations,
            errorRate = errorRate
        )
    }
    
    inline fun <T> measureOperation(
        operation: String,
        category: String = "general",
        block: () -> T
    ): T {
        val operationStartTime = System.currentTimeMillis()
        return try {
            val result = block()
            val duration = System.currentTimeMillis() - operationStartTime
            recordOperation(operation, duration, true, category)
            result
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - operationStartTime
            recordOperation(operation, duration, false, category)
            throw e
        }
    }
    
    suspend fun <T> measureSuspendOperation(
        operation: String,
        category: String = "general",
        block: suspend () -> T
    ): T {
        val startTime = System.currentTimeMillis()
        return try {
            val result = block()
            val duration = System.currentTimeMillis() - startTime
            recordOperation(operation, duration, true, category)
            result
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            recordOperation(operation, duration, false, category)
            throw e
        }
    }
    
    fun startMonitoring(context: Context) {
        scope.launch {
            while (isActive) {
                try {
                    captureMemorySnapshot(context)
                    delay(30000)
                } catch (e: Exception) {
                    Log.e(TAG, "性能监控异常", e)
                    delay(60000)
                }
            }
        }
    }
    
    fun getPerformanceReport(): String {
        val state = _performanceState.value
        return buildString {
            appendLine("=== 高级性能监控报告 ===")
            appendLine("会话ID: ${state.sessionId}")
            appendLine("会话时长: ${state.sessionDuration / 1000}秒")
            appendLine("总操作数: ${state.totalOperations}")
            appendLine("错误率: ${"%.2f".format(state.errorRate)}%")
            
            state.currentMemory?.let { memory ->
                appendLine("当前内存使用: ${"%.1f".format(memory.usagePercent)}%")
                appendLine("内存使用: ${memory.usedMemory / 1024 / 1024}MB / ${memory.maxMemory / 1024 / 1024}MB")
            }
            
            appendLine("平均内存使用: ${"%.1f".format(state.averageMemoryUsage)}%")
            
            appendLine("\n操作性能统计:")
            state.metrics.values.sortedByDescending { it.averageTime }.take(10).forEach { metric ->
                appendLine("  ${metric.operation}:")
                appendLine("    平均耗时: ${"%.1f".format(metric.averageTime)}ms")
                appendLine("    调用次数: ${metric.count}")
                appendLine("    成功率: ${"%.1f".format(metric.successRate)}%")
                appendLine("    P95: ${metric.p95Time}ms, P99: ${metric.p99Time}ms")
            }
            
            appendLine("\n渲染性能统计:")
            state.renderMetrics.values.forEach { metric ->
                appendLine("  ${metric.screen}:")
                appendLine("    平均渲染时间: ${"%.1f".format(metric.averageTime)}ms")
                appendLine("    跳过帧数: ${metric.skippedFrames}")
                appendLine("    卡顿次数: ${metric.jankCount}")
            }
            
            appendLine("============================")
        }
    }
    
    fun clearMetrics() {
        performanceMetrics.clear()
        renderMetrics.clear()
        memorySnapshots.clear()
        updatePerformanceState()
        Log.i(TAG, "性能指标已清理")
    }
}

@Composable
fun PerformanceTracker(
    screenName: String,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
) {
    var operationStartTime by remember { mutableStateOf(0L) }
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    operationStartTime = System.currentTimeMillis()
                    AdvancedPerformanceMonitor.recordRenderTime(screenName, 0)
                }
                Lifecycle.Event.ON_PAUSE -> {
                    val duration = System.currentTimeMillis() - operationStartTime
                    AdvancedPerformanceMonitor.recordRenderTime(screenName, duration)
                }
                else -> {}
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}