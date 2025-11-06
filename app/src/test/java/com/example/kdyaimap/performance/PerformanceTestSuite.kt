package com.example.kdyaimap.performance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kdyaimap.util.AdvancedPerformanceMonitor
import com.example.kdyaimap.util.CacheManager
import com.example.kdyaimap.util.CoroutineManager
import com.example.kdyaimap.util.ErrorHandler
import com.example.kdyaimap.util.MemoryLeakProtector
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class PerformanceTestSuite {
    
    private lateinit var context: Context
    private lateinit var cacheManager: CacheManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        cacheManager = CacheManager(context)
        AdvancedPerformanceMonitor.startNewSession()
        ErrorHandler.clearErrors()
    }
    
    @Test
    fun testCacheManagerPerformance() {
        val testData = (1..1000).map { "item_$it" }
        
        val writeTime = measureTimeMillis {
            runBlocking {
                testData.forEach { item ->
                    cacheManager.put("test_$item", item)
                }
            }
        }
        
        println("缓存写入1000项耗时: ${writeTime}ms")
        assert(writeTime < 1000) { "缓存写入性能不达标: ${writeTime}ms > 1000ms" }
        
        val readTime = measureTimeMillis {
            runBlocking {
                testData.forEach { item ->
                    cacheManager.get("test_$item") { item }
                }
            }
        }
        
        println("缓存读取1000项耗时: ${readTime}ms")
        assert(readTime < 500) { "缓存读取性能不达标: ${readTime}ms > 500ms" }
    }
    
    @Test
    fun testCoroutineManagerPerformance() {
        val jobCount = 1000
        val startTime = System.currentTimeMillis()
        
        runBlocking {
            repeat(jobCount) { index ->
                CoroutineManager.launchIO("performance_test_$index") {
                    delay(10)
                }
            }
            
            delay(2000)
        }
        
        val totalTime = System.currentTimeMillis() - startTime
        println("协程管理器执行${jobCount}个任务耗时: ${totalTime}ms")
        assert(totalTime < 3000) { "协程管理器性能不达标: ${totalTime}ms > 3000ms" }
        
        val report = CoroutineManager.getCoroutineReport()
        println(report)
    }
    
    @Test
    fun testMemoryLeakProtectorPerformance() {
        val testObjects = (1..100).map { TestObject(it) }
        
        val trackingTime = measureTimeMillis {
            testObjects.forEach { obj ->
                MemoryLeakProtector.trackObject("test_${obj.id}", obj)
            }
        }
        
        println("内存泄漏防护器跟踪100个对象耗时: ${trackingTime}ms")
        assert(trackingTime < 100) { "内存跟踪性能不达标: ${trackingTime}ms > 100ms" }
        
        val cleanupTime = measureTimeMillis {
            testObjects.forEach { obj ->
                MemoryLeakProtector.untrackObject("test_${obj.id}")
            }
        }
        
        println("内存泄漏防护器清理100个对象耗时: ${cleanupTime}ms")
        assert(cleanupTime < 50) { "内存清理性能不达标: ${cleanupTime}ms > 50ms" }
    }
    
    @Test
    fun testAdvancedPerformanceMonitorOverhead() {
        val operationCount = 1000
        
        val baselineTime = measureTimeMillis {
            repeat(operationCount) { index ->
                simulateWork(index)
            }
        }
        
        val monitoredTime = measureTimeMillis {
            repeat(operationCount) { index ->
                AdvancedPerformanceMonitor.measureOperation("test_operation", "performance") {
                    simulateWork(index)
                }
            }
        }
        
        val overhead = monitoredTime - baselineTime
        val overheadPercent = (overhead.toDouble() / baselineTime * 100)
        
        println("性能监控开销: ${overhead}ms (${"%.2f".format(overheadPercent)}%)")
        assert(overheadPercent < 10.0) { "性能监控开销过高: ${overheadPercent}% > 10%" }
        
        val report = AdvancedPerformanceMonitor.getPerformanceReport()
        println(report)
    }
    
    @Test
    fun testErrorHandlerPerformance() {
        val errorCount = 100
        
        val handlingTime = measureTimeMillis {
            repeat(errorCount) { index ->
                try {
                    if (index % 10 == 0) {
                        throw RuntimeException("测试异常 $index")
                    }
                } catch (e: Exception) {
                    ErrorHandler.handleError(e, mapOf("index" to index))
                }
            }
        }
        
        println("错误处理器处理${errorCount}个操作耗时: ${handlingTime}ms")
        assert(handlingTime < 500) { "错误处理性能不达标: ${handlingTime}ms > 500ms" }
        
        val report = ErrorHandler.getErrorReport()
        println(report)
    }
    
    @Test
    fun testMemoryUsageUnderLoad() {
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        runBlocking {
            val largeDataList = mutableListOf<ByteArray>()
            
            repeat(100) {
                largeDataList.add(ByteArray(1024 * 10))
                delay(10)
            }
            
            largeDataList.clear()
            System.gc()
            delay(100)
        }
        
        val finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryIncrease = finalMemory - initialMemory
        val memoryIncreaseMB = memoryIncrease / 1024 / 1024
        
        println("内存增长: ${memoryIncreaseMB}MB")
        assert(memoryIncreaseMB < 50) { "内存使用增长过多: ${memoryIncreaseMB}MB > 50MB" }
        
        val memoryReport = MemoryLeakProtector.getMemoryReport()
        println(memoryReport)
    }
    
    @Test
    fun testConcurrentAccessPerformance() {
        val threadCount = 10
        val operationsPerThread = 100
        
        val totalTime = measureTimeMillis {
            val threads = (1..threadCount).map { threadId ->
                Thread {
                    repeat(operationsPerThread) { opId ->
                        AdvancedPerformanceMonitor.measureOperation("concurrent_op_${threadId}_${opId}", "concurrent") {
                            Thread.sleep(1)
                        }
                    }
                }
            }
            
            threads.forEach { it.start() }
            threads.forEach { it.join() }
        }
        
        val totalOperations = threadCount * operationsPerThread
        val avgTimePerOperation = totalTime.toDouble() / totalOperations
        
        println("并发执行${totalOperations}个操作总耗时: ${totalTime}ms")
        println("平均每个操作耗时: ${"%.3f".format(avgTimePerOperation)}ms")
        
        assert(avgTimePerOperation < 5.0) { "并发性能不达标: 平均${avgTimePerOperation}ms > 5ms" }
    }
    
    @Test
    fun testCacheEfficiencyUnderLoad() {
        val keys = (1..100).map { "key_$it" }
        val accessPattern = generateSequence { keys.random() }.take(1000)
        
        val cacheHitRate = runBlocking {
            var hits = 0
            var total = 0
            
            accessPattern.forEach { key ->
                val result = cacheManager.get(key) { 
                    total++
                    "value_for_$key"
                }
                if (result != "value_for_$key") {
                    hits++
                }
            }
            
            if (total == 0) 0.0 else (hits.toDouble() / total * 100)
        }
        
        println("缓存命中率: ${"%.2f".format(cacheHitRate)}%")
        assert(cacheHitRate > 80.0) { "缓存命中率过低: ${cacheHitRate}% < 80%" }
        
        val cacheInfo = cacheManager.getCacheInfo()
        println("缓存信息: $cacheInfo")
    }
    
    private fun simulateWork(index: Int) {
        var sum = 0
        for (i in 1..100) {
            sum += i * index
        }
    }
    
    private data class TestObject(val id: Int)
}