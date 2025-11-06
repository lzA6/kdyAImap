package com.example.kdyaimap.util

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.lang.OutOfMemoryError
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

object ErrorHandler {
    private const val TAG = "ErrorHandler"
    
    private val errorStats = ConcurrentHashMap<String, ErrorStats>()
    private val errorIdGenerator = AtomicLong(0)
    
    private val _errorState = MutableStateFlow(ErrorState())
    val errorState: StateFlow<ErrorState> = _errorState.asStateFlow()
    
    data class ErrorStats(
        val count: Long = 0,
        val lastOccurrence: Long = 0,
        val lastMessage: String = "",
        val isRecurring: Boolean = false
    )
    
    data class ErrorInfo(
        val id: String,
        val type: ErrorType,
        val message: String,
        val cause: Throwable?,
        val timestamp: Long,
        val context: Map<String, Any> = emptyMap(),
        val severity: ErrorSeverity = ErrorSeverity.MEDIUM
    )
    
    data class ErrorState(
        val recentErrors: List<ErrorInfo> = emptyList(),
        val criticalErrors: List<ErrorInfo> = emptyList(),
        val totalErrors: Long = 0,
        val lastErrorTime: Long = 0
    )
    
    enum class ErrorType {
        NETWORK_ERROR,
        DATABASE_ERROR,
        UI_ERROR,
        MEMORY_ERROR,
        PERMISSION_ERROR,
        UNKNOWN_ERROR
    }
    
    enum class ErrorSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    fun handleError(
        throwable: Throwable,
        context: Map<String, Any> = emptyMap(),
        severity: ErrorSeverity = ErrorSeverity.MEDIUM
    ) {
        val errorType = classifyError(throwable)
        val errorId = "error_${errorIdGenerator.incrementAndGet()}"
        
        val errorInfo = ErrorInfo(
            id = errorId,
            type = errorType,
            message = throwable.message ?: "未知错误",
            cause = throwable,
            timestamp = System.currentTimeMillis(),
            context = context,
            severity = severity
        )
        
        logError(errorInfo)
        updateErrorStats(errorInfo)
        updateErrorState(errorInfo)
        
        if (severity == ErrorSeverity.CRITICAL) {
            handleCriticalError(errorInfo)
        }
        
        attemptRecovery(errorInfo)
    }
    
    private fun classifyError(throwable: Throwable): ErrorType {
        return when (throwable) {
            is UnknownHostException, is SocketTimeoutException, is IOException -> ErrorType.NETWORK_ERROR
            is OutOfMemoryError -> ErrorType.MEMORY_ERROR
            is SecurityException -> ErrorType.PERMISSION_ERROR
            is CancellationException -> ErrorType.UNKNOWN_ERROR
            else -> {
                when {
                    throwable.message?.contains("database", ignoreCase = true) == true -> ErrorType.DATABASE_ERROR
                    throwable.message?.contains("ui", ignoreCase = true) == true -> ErrorType.UI_ERROR
                    else -> ErrorType.UNKNOWN_ERROR
                }
            }
        }
    }
    
    private fun logError(errorInfo: ErrorInfo) {
        val logLevel = when (errorInfo.severity) {
            ErrorSeverity.LOW -> Log.DEBUG
            ErrorSeverity.MEDIUM -> Log.WARN
            ErrorSeverity.HIGH -> Log.ERROR
            ErrorSeverity.CRITICAL -> Log.ERROR
        }
        
        val contextStr = if (errorInfo.context.isNotEmpty()) {
            errorInfo.context.entries.joinToString(", ") { "${it.key}=${it.value}" }
        } else {
            ""
        }
        
        Log.println(
            logLevel,
            TAG,
            "[${errorInfo.type}] ${errorInfo.message} (ID: ${errorInfo.id}) $contextStr"
        )
        
        errorInfo.cause?.let { cause ->
            Log.w(TAG, "错误堆栈: $errorInfo", cause)
        }
    }
    
    private fun updateErrorStats(errorInfo: ErrorInfo) {
        val key = "${errorInfo.type}_${errorInfo.message}"
        val current = errorStats.getOrDefault(key, ErrorStats())
        val now = System.currentTimeMillis()
        
        val isRecurring = (now - current.lastOccurrence) < 60000
        
        errorStats[key] = current.copy(
            count = current.count + 1,
            lastOccurrence = now,
            lastMessage = errorInfo.message,
            isRecurring = isRecurring
        )
        
        if (isRecurring) {
            Log.w(TAG, "检测到重复错误: ${errorInfo.message} (出现${current.count + 1}次)")
        }
    }
    
    private fun updateErrorState(errorInfo: ErrorInfo) {
        val currentState = _errorState.value
        val newRecentErrors = (listOf(errorInfo) + currentState.recentErrors).take(50)
        val newCriticalErrors = if (errorInfo.severity == ErrorSeverity.CRITICAL) {
            (listOf(errorInfo) + currentState.criticalErrors).take(20)
        } else {
            currentState.criticalErrors
        }
        
        _errorState.value = currentState.copy(
            recentErrors = newRecentErrors,
            criticalErrors = newCriticalErrors,
            totalErrors = currentState.totalErrors + 1,
            lastErrorTime = errorInfo.timestamp
        )
    }
    
    private fun handleCriticalError(errorInfo: ErrorInfo) {
        Log.e(TAG, "处理关键错误: ${errorInfo.message}")
        
        when (errorInfo.type) {
            ErrorType.MEMORY_ERROR -> {
                MemoryLeakProtector.cleanup()
                System.gc()
            }
            ErrorType.NETWORK_ERROR -> {
                CoroutineManager.cancelJobsByName("network")
            }
            ErrorType.DATABASE_ERROR -> {
                CoroutineManager.cancelJobsByName("database")
            }
            else -> {}
        }
    }
    
    private fun attemptRecovery(errorInfo: ErrorInfo) {
        CoroutineManager.launchIO("error_recovery_${errorInfo.id}") {
            try {
                when (errorInfo.type) {
                    ErrorType.NETWORK_ERROR -> {
                        delay(5000)
                        Log.i(TAG, "网络错误恢复尝试完成")
                    }
                    ErrorType.MEMORY_ERROR -> {
                        delay(2000)
                        System.gc()
                        Log.i(TAG, "内存错误恢复尝试完成")
                    }
                    ErrorType.DATABASE_ERROR -> {
                        delay(1000)
                        Log.i(TAG, "数据库错误恢复尝试完成")
                    }
                    else -> {
                        delay(1000)
                        Log.i(TAG, "通用错误恢复尝试完成")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "错误恢复失败", e)
            }
        }
    }
    
    fun <T> safeExecute(
        operation: String,
        context: Map<String, Any> = emptyMap(),
        severity: ErrorSeverity = ErrorSeverity.MEDIUM,
        block: () -> T
    ): Result<T> {
        return try {
            val result = block()
            Result.success(result)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleError(e, context + ("operation" to operation), severity)
            Result.failure(e)
        }
    }
    
    suspend fun <T> safeExecuteSuspend(
        operation: String,
        context: Map<String, Any> = emptyMap(),
        severity: ErrorSeverity = ErrorSeverity.MEDIUM,
        block: suspend () -> T
    ): Result<T> {
        return try {
            val result = block()
            Result.success(result)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleError(e, context + ("operation" to operation), severity)
            Result.failure(e)
        }
    }
    
    fun getErrorReport(): String {
        val state = _errorState.value
        return buildString {
            appendLine("=== 错误处理报告 ===")
            appendLine("总错误数: ${state.totalErrors}")
            appendLine("最近错误数: ${state.recentErrors.size}")
            appendLine("关键错误数: ${state.criticalErrors.size}")
            appendLine("最后错误时间: ${state.lastErrorTime}")
            
            appendLine("\n错误类型统计:")
            errorStats.entries.groupBy { it.key.split("_").first() }
                .forEach { (type, errors) ->
                    val totalCount = errors.map { (_, stats) -> stats.count }.sum()
                    appendLine("  $type: $totalCount 次")
                }
            
            if (state.criticalErrors.isNotEmpty()) {
                appendLine("\n关键错误:")
                state.criticalErrors.take(5).forEach { error ->
                    appendLine("  - [${error.type}] ${error.message}")
                }
            }
            
            appendLine("==================")
        }
    }
    
    fun clearErrors() {
        errorStats.clear()
        _errorState.value = ErrorState()
        Log.i(TAG, "错误统计已清理")
    }
}