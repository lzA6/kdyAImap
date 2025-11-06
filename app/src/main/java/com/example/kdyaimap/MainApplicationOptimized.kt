package com.example.kdyaimap

import android.app.Application
import android.content.ComponentCallbacks2
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.*
import com.example.kdyaimap.util.ErrorHandler
import com.example.kdyaimap.util.AdvancedPerformanceMonitor
import com.example.kdyaimap.util.CoroutineManager
import com.example.kdyaimap.util.PrivacyComplianceManager
import com.example.kdyaimap.util.MemoryLeakProtector

/**
 * 优化的应用程序类
 * 简化版本，专注于核心性能优化
 */
@HiltAndroidApp
class MainApplicationOptimized : Application() {
    
    private val TAG = "MainApplicationOptimized"
    
    // 性能监控任务
    private var performanceMonitorJob: String? = null
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // 初始化错误处理器
            ErrorHandler.clearErrors()
            
            // 初始化高级性能监控
            AdvancedPerformanceMonitor.startNewSession()
            AdvancedPerformanceMonitor.startMonitoring(this)
            
            // 初始化隐私合规
            initializePrivacyCompliance()
            
            // 初始化内存泄漏防护
            MemoryLeakProtector.startMemoryMonitoring(this@MainApplicationOptimized)
            
            // 初始化性能监控
            initializePerformanceMonitoring()
            
            Log.i(TAG, "应用启动完成，性能优化系统已激活")
            
        } catch (e: Exception) {
            ErrorHandler.handleError(e, mapOf("phase" to "application_init"), ErrorHandler.ErrorSeverity.CRITICAL)
            Log.e(TAG, "应用初始化失败", e)
        }
    }
    
    /**
     * 初始化性能监控系统
     */
    private fun initializePerformanceMonitoring() {
        performanceMonitorJob = "performance_monitor"
        CoroutineManager.launchIO(performanceMonitorJob!!) {
            while (CoroutineScope(SupervisorJob()).isActive) {
                ErrorHandler.safeExecuteSuspend("memory_usage_check") {
                    delay(30_000L)
                    checkMemoryUsage()
                }.onFailure { exception ->
                    Log.e(TAG, "性能监控异常", exception)
                }
            }
        }
        
        Log.d(TAG, "性能监控系统已启动")
    }
    
    private fun initializePrivacyCompliance() {
        CoroutineManager.launchIO("privacy_compliance_init") {
            try {
                PrivacyComplianceManager.initializePrivacyCompliance(this@MainApplicationOptimized)
                Log.d(TAG, "隐私合规系统已启动")
                
            } catch (e: Exception) {
                Log.e(TAG, "隐私合规初始化失败", e)
            }
        }
        
    }
    
    private fun initializeMemoryLeakProtection() {
        ErrorHandler.safeExecute("memory_leak_protection_init") {
            MemoryLeakProtector.startMemoryMonitoring(this@MainApplicationOptimized)
            Log.d(TAG, "内存泄漏防护系统已启动")
        }.onFailure { exception ->
            Log.e(TAG, "内存泄漏防护初始化失败", exception)
        }
    }
    
    /**
     * 检查内存使用情况
     */
    private suspend fun checkMemoryUsage() {
        CoroutineManager.withDefaultContext("memory_check") {
            AdvancedPerformanceMonitor.measureOperation("memory_usage_check", "system") {
                val runtime = Runtime.getRuntime()
                val totalMemory = runtime.totalMemory()
                val freeMemory = runtime.freeMemory()
                val usedMemory = totalMemory - freeMemory
                val maxMemory = runtime.maxMemory()
                
                val memoryUsagePercent = (usedMemory.toDouble() / maxMemory * 100)
                
                Log.d(TAG, "内存使用: ${"%.1f".format(memoryUsagePercent)}% " +
                        "(${usedMemory / 1024 / 1024}MB / ${maxMemory / 1024 / 1024}MB)")
                
                if (memoryUsagePercent > 85) {
                    Log.w(TAG, "高内存使用警告: ${"%.1f".format(memoryUsagePercent)}%")
                    System.gc()
                }
            }
        }
    }
    
    /**
     * 处理低内存情况
     */
    override fun onLowMemory() {
        super.onLowMemory()
        
        Log.w(TAG, "系统内存不足，触发清理机制")
        
        CoroutineManager.launchIO("low_memory_cleanup") {
            ErrorHandler.safeExecuteSuspend("low_memory_cleanup") {
                System.gc()
                
                delay(1000L)
                checkMemoryUsage()
                
                Log.i(TAG, "低内存清理完成")
            }.onFailure { exception ->
                Log.e(TAG, "低内存清理失败", exception)
            }
        }
    }
    
    /**
     * 处理内存整理
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        
        @Suppress("DEPRECATION")
        val levelName = when (level) {
            android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> "运行时关键内存不足"
            android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> "运行时内存不足"
            android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> "运行时内存适中"
            android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> "UI隐藏"
            android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> "后台运行"
            android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> "内存完全清理"
            else -> "未知级别: $level"
        }
        
        Log.d(TAG, "内存整理: $levelName")
        
        @Suppress("DEPRECATION")
        when (level) {
            android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL, android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                CoroutineManager.launchIO("performance_monitoring") {
                    System.gc()
                }
            }
            android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> {
                CoroutineManager.launchIO("error_monitoring") {
                    checkMemoryUsage()
                }
            }
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        
        Log.i(TAG, "应用终止，清理资源")
        
        ErrorHandler.safeExecute("application_cleanup") {
            performanceMonitorJob?.let { CoroutineManager.cancelJob(it) }
            
            CoroutineManager.cleanup()
            MemoryLeakProtector.cleanup()
            AdvancedPerformanceMonitor.clearMetrics()
            
            Log.i(TAG, "应用资源清理完成")
        }.onFailure { exception ->
            Log.e(TAG, "资源清理失败", exception)
        }
    }
}

/**
 * 应用性能监控扩展
 */
object AppPerformanceTracker {
    
    private val TAG = "AppPerformanceTracker"
    
    /**
     * 跟踪页面加载性能
     */
    fun trackPageLoad(pageName: String, loadTime: Long) {
        Log.d(TAG, "页面加载: $pageName 耗时 ${loadTime}ms")
        
        if (loadTime > 2000) {
            Log.w(TAG, "页面加载过慢: $pageName (${loadTime}ms)")
        }
    }
    
    /**
     * 跟踪用户操作性能
     */
    fun trackUserAction(actionName: String, duration: Long) {
        Log.d(TAG, "用户操作: $actionName 耗时 ${duration}ms")
    }
    
    /**
     * 跟踪网络请求性能
     */
    fun trackNetworkRequest(url: String, success: Boolean, duration: Long) {
        Log.d(TAG, "网络请求: $url - ${if (success) "成功" else "失败"} (${duration}ms)")
    }
    
    /**
     * 跟踪数据库操作性能
     */
    fun trackDatabaseOperation(operation: String, duration: Long) {
        Log.d(TAG, "数据库操作: $operation 耗时 ${duration}ms")
        
        if (duration > 1000) {
            Log.w(TAG, "数据库操作过慢: $operation (${duration}ms)")
        }
    }
}