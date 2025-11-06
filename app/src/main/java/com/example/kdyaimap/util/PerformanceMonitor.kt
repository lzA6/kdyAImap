package com.example.kdyaimap.util

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

object PerformanceMonitor {
    private const val TAG = "PerformanceMonitor"
    
    private val metrics = ConcurrentHashMap<String, PerformanceMetric>()
    private val _performanceState = MutableStateFlow(PerformanceState())
    val performanceState: StateFlow<PerformanceState> = _performanceState.asStateFlow()
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    data class PerformanceMetric(
        var count: Long = 0,
        var totalTime: Long = 0,
        var minTime: Long = Long.MAX_VALUE,
        var maxTime: Long = 0,
        var lastTime: Long = 0
    ) {
        val averageTime: Double get() = if (count > 0) totalTime.toDouble() / count else 0.0
    }
    
    data class PerformanceState(
        val renderMetrics: Map<String, PerformanceMetric> = emptyMap(),
        val databaseMetrics: Map<String, PerformanceMetric> = emptyMap(),
        val networkMetrics: Map<String, PerformanceMetric> = emptyMap(),
        val memoryUsage: MemoryUsage = MemoryUsage()
    )
    
    data class MemoryUsage(
        val usedMemory: Long = 0,
        val maxMemory: Long = 0,
        val availableMemory: Long = 0
    )
    
    fun startTiming(operation: String): Long {
        return System.currentTimeMillis()
    }
    
    fun endTiming(operation: String, startTime: Long, category: String = "general") {
        val duration = System.currentTimeMillis() - startTime
        recordMetric(operation, duration, category)
    }
    
    fun recordMetric(operation: String, duration: Long, category: String) {
        scope.launch {
            val metric = metrics.getOrPut("${category}_$operation") { PerformanceMetric() }
            metric.count++
            metric.totalTime += duration
            metric.minTime = minOf(metric.minTime, duration)
            metric.maxTime = maxOf(metric.maxTime, duration)
            metric.lastTime = duration
            
            updatePerformanceState()
            
            if (duration > 1000) {
                Log.w(TAG, "Slow operation detected: $operation took ${duration}ms")
            }
        }
    }
    
    private fun updatePerformanceState() {
        val renderMetrics = metrics.filterKeys { it.startsWith("render_") }
            .mapKeys { it.key.removePrefix("render_") }
        
        val databaseMetrics = metrics.filterKeys { it.startsWith("database_") }
            .mapKeys { it.key.removePrefix("database_") }
        
        val networkMetrics = metrics.filterKeys { it.startsWith("network_") }
            .mapKeys { it.key.removePrefix("network_") }
        
        val runtime = Runtime.getRuntime()
        val memoryUsage = MemoryUsage(
            usedMemory = runtime.totalMemory() - runtime.freeMemory(),
            maxMemory = runtime.maxMemory(),
            availableMemory = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory())
        )
        
        _performanceState.value = PerformanceState(
            renderMetrics = renderMetrics,
            databaseMetrics = databaseMetrics,
            networkMetrics = networkMetrics,
            memoryUsage = memoryUsage
        )
    }
    
    inline fun <T> measureRenderTime(operation: String, block: () -> T): T {
        val startTime = startTiming("render_$operation")
        return try {
            block()
        } finally {
            endTiming("render_$operation", startTime, "render")
        }
    }
    
    inline fun <T> measureDatabaseTime(operation: String, block: () -> T): T {
        val startTime = startTiming("database_$operation")
        return try {
            block()
        } finally {
            endTiming("database_$operation", startTime, "database")
        }
    }
    
    inline fun <T> measureNetworkTime(operation: String, block: () -> T): T {
        val startTime = startTiming("network_$operation")
        return try {
            block()
        } finally {
            endTiming("network_$operation", startTime, "network")
        }
    }
    
    fun logPerformanceReport() {
        scope.launch {
            val state = _performanceState.value
            Log.i(TAG, "=== Performance Report ===")
            
            Log.i(TAG, "Render Metrics:")
            state.renderMetrics.forEach { (operation, metric) ->
                Log.i(TAG, "  $operation: avg=${metric.averageTime.toInt()}ms, count=${metric.count}")
            }
            
            Log.i(TAG, "Database Metrics:")
            state.databaseMetrics.forEach { (operation, metric) ->
                Log.i(TAG, "  $operation: avg=${metric.averageTime.toInt()}ms, count=${metric.count}")
            }
            
            Log.i(TAG, "Memory Usage: ${state.memoryUsage.usedMemory / 1024 / 1024}MB / ${state.memoryUsage.maxMemory / 1024 / 1024}MB")
            Log.i(TAG, "=== End Report ===")
        }
    }
    
    fun clearMetrics() {
        scope.launch {
            metrics.clear()
            updatePerformanceState()
        }
    }
}

@Composable
fun PerformanceTracker(
    operation: String,
    lifecycleOwner: LifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current,
    onTimeMeasured: (Long) -> Unit = {}
) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val startTime = PerformanceMonitor.startTiming("render_$operation")
                onTimeMeasured(startTime)
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

inline fun <T> measurePerformance(operation: String, block: () -> T): T {
    val time = measureTimeMillis {
        return block()
    }
    PerformanceMonitor.recordMetric(operation, time, "manual")
    return block()
}