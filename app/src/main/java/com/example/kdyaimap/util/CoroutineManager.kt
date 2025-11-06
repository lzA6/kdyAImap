package com.example.kdyaimap.util

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

object CoroutineManager {
    private const val TAG = "CoroutineManager"
    
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val defaultScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val activeJobs = ConcurrentHashMap<String, JobInfo>()
    private val jobIdGenerator = AtomicLong(0)
    
    private val _coroutineStats = MutableStateFlow(CoroutineStats())
    val coroutineStats: StateFlow<CoroutineStats> = _coroutineStats.asStateFlow()
    
    data class JobInfo(
        val id: String,
        val name: String,
        val startTime: Long,
        val scope: String,
        val isCompleted: Boolean = false
    )
    
    data class CoroutineStats(
        val totalJobs: Long = 0,
        val activeJobs: Int = 0,
        val completedJobs: Long = 0,
        val failedJobs: Long = 0,
        val averageExecutionTime: Long = 0
    )
    
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        when (exception) {
            is CancellationException -> {
                Log.d(TAG, "协程被取消: ${exception.message}")
            }
            else -> {
                Log.e(TAG, "协程异常: ${exception.message}", exception)
                updateStats { it.copy(failedJobs = it.failedJobs + 1) }
            }
        }
    }
    
    fun launchMain(
        name: String,
        block: suspend CoroutineScope.() -> Unit
    ) = launchInScope(mainScope, "Main", name, block)
    
    fun launchIO(
        name: String,
        block: suspend CoroutineScope.() -> Unit
    ) = launchInScope(ioScope, "IO", name, block)
    
    fun launchDefault(
        name: String,
        block: suspend CoroutineScope.() -> Unit
    ) = launchInScope(defaultScope, "Default", name, block)
    
    private fun launchInScope(
        scope: CoroutineScope,
        scopeType: String,
        name: String,
        block: suspend CoroutineScope.() -> Unit
    ) {
        val jobId = "job_${jobIdGenerator.incrementAndGet()}"
        val jobInfo = JobInfo(jobId, name, System.currentTimeMillis(), scopeType)
        
        activeJobs[jobId] = jobInfo
        updateStats { it.copy(totalJobs = it.totalJobs + 1, activeJobs = activeJobs.size) }
        
        val job = scope.launch(exceptionHandler) {
            try {
                block()
                Log.d(TAG, "协程完成: $name")
                completeJob(jobId)
            } catch (e: Exception) {
                Log.e(TAG, "协程执行失败: $name", e)
                failJob(jobId)
                throw e
            }
        }
        
        job.invokeOnCompletion {
            if (it == null) {
                completeJob(jobId)
            } else if (it !is CancellationException) {
                failJob(jobId)
            }
        }
    }
    
    suspend fun <T> withIOContext(
        name: String,
        block: suspend CoroutineScope.() -> T
    ): T = withContext(Dispatchers.IO + exceptionHandler) {
        try {
            val startTime = System.currentTimeMillis()
            val result = block()
            val executionTime = System.currentTimeMillis() - startTime
            Log.d(TAG, "IO操作完成: $name, 耗时: ${executionTime}ms")
            result
        } catch (e: Exception) {
            Log.e(TAG, "IO操作失败: $name", e)
            throw e
        }
    }
    
    suspend fun <T> withDefaultContext(
        name: String,
        block: suspend CoroutineScope.() -> T
    ): T = withContext(Dispatchers.Default + exceptionHandler) {
        try {
            val startTime = System.currentTimeMillis()
            val result = block()
            val executionTime = System.currentTimeMillis() - startTime
            Log.d(TAG, "计算操作完成: $name, 耗时: ${executionTime}ms")
            result
        } catch (e: Exception) {
            Log.e(TAG, "计算操作失败: $name", e)
            throw e
        }
    }
    
    private fun completeJob(jobId: String) {
        activeJobs[jobId]?.let { jobInfo ->
            val completedJobInfo = jobInfo.copy(isCompleted = true)
            activeJobs[jobId] = completedJobInfo
            
            val executionTime = System.currentTimeMillis() - jobInfo.startTime
            updateStats { 
                it.copy(
                    completedJobs = it.completedJobs + 1,
                    activeJobs = activeJobs.size,
                    averageExecutionTime = (it.averageExecutionTime * it.completedJobs + executionTime) / (it.completedJobs + 1)
                )
            }
            
            activeJobs.remove(jobId)
        }
    }
    
    private fun failJob(jobId: String) {
        activeJobs.remove(jobId)
        updateStats { it.copy(activeJobs = activeJobs.size) }
    }
    
    private fun updateStats(update: (CoroutineStats) -> CoroutineStats) {
        _coroutineStats.value = update(_coroutineStats.value)
    }
    
    fun cancelJob(jobId: String) {
        activeJobs.remove(jobId)
        updateStats { it.copy(activeJobs = activeJobs.size) }
        Log.d(TAG, "协程已取消: $jobId")
    }
    
    fun cancelJobsByName(name: String) {
        val jobsToCancel = activeJobs.filter { it.value.name == name }.keys
        jobsToCancel.forEach { cancelJob(it) }
        Log.d(TAG, "已取消 ${jobsToCancel.size} 个名为 $name 的协程")
    }
    
    fun getActiveJobsCount(): Int = activeJobs.size
    
    fun getActiveJobsInfo(): List<JobInfo> = activeJobs.values.toList()
    
    fun getCoroutineReport(): String {
        val stats = _coroutineStats.value
        return buildString {
            appendLine("=== 协程使用报告 ===")
            appendLine("总任务数: ${stats.totalJobs}")
            appendLine("活跃任务数: ${stats.activeJobs}")
            appendLine("已完成任务数: ${stats.completedJobs}")
            appendLine("失败任务数: ${stats.failedJobs}")
            appendLine("平均执行时间: ${stats.averageExecutionTime}ms")
            appendLine("当前活跃任务:")
            activeJobs.values.forEach { job ->
                val runtime = System.currentTimeMillis() - job.startTime
                appendLine("  - ${job.name} (${job.scope}): 运行 ${runtime}ms")
            }
            appendLine("==================")
        }
    }
    
    fun cleanup() {
        mainScope.cancel()
        ioScope.cancel()
        defaultScope.cancel()
        activeJobs.clear()
        Log.i(TAG, "协程管理器已清理")
    }
}