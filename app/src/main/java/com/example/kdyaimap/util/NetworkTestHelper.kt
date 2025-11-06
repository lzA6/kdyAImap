package com.example.kdyaimap.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 网络测试助手
 * 用于诊断网络连接问题
 */
@Singleton
class NetworkTestHelper @Inject constructor() {
    
    private val TAG = "NetworkTestHelper"
    
    /**
     * 测试基础网络连接
     */
    suspend fun testBasicConnectivity(): NetworkTestResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "开始测试基础网络连接")
                
                // 测试一个简单的国内网站
                val url = URL("https://www.baidu.com")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseCode = connection.responseCode
                Log.d(TAG, "百度连接测试结果: $responseCode")
                
                if (responseCode in 200..299) {
                    NetworkTestResult.Success("基础网络连接正常")
                } else {
                    NetworkTestResult.Failure("基础网络连接异常: HTTP $responseCode")
                }
            } catch (e: Exception) {
                Log.e(TAG, "基础网络连接测试失败", e)
                NetworkTestResult.Failure("基础网络连接失败: ${e.message}")
            }
        }
    }
    
    /**
     * 测试API服务器连接
     */
    suspend fun testApiConnectivity(): NetworkTestResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "开始测试API服务器连接")
                
                val apiUrl = "https://my-map-backend.tfai.workers.dev/"
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                
                val responseCode = connection.responseCode
                Log.d(TAG, "API服务器连接测试结果: $responseCode")
                
                if (responseCode in 200..299) {
                    NetworkTestResult.Success("API服务器连接正常")
                } else if (responseCode == 404) {
                    NetworkTestResult.Success("API服务器可访问（404是正常的，因为我们访问的是根路径）")
                } else {
                    NetworkTestResult.Failure("API服务器连接异常: HTTP $responseCode")
                }
            } catch (e: Exception) {
                Log.e(TAG, "API服务器连接测试失败", e)
                val errorMessage = e.message ?: "未知错误"
                
                when {
                    errorMessage.contains("timeout") -> {
                        NetworkTestResult.Failure("API服务器连接超时，可能需要代理")
                    }
                    errorMessage.contains("UnknownHost") -> {
                        NetworkTestResult.Failure("无法解析API服务器地址，可能需要DNS或代理")
                    }
                    errorMessage.contains("Connection") -> {
                        NetworkTestResult.Failure("无法连接到API服务器，可能需要代理")
                    }
                    else -> {
                        NetworkTestResult.Failure("API服务器连接失败: $errorMessage")
                    }
                }
            }
        }
    }
    
    /**
     * 运行完整的网络诊断
     */
    suspend fun runFullDiagnosis(): NetworkDiagnosisReport {
        Log.d(TAG, "开始运行完整网络诊断")
        
        val basicResult = testBasicConnectivity()
        val apiResult = testApiConnectivity()
        
        val report = NetworkDiagnosisReport(
            basicConnectivity = basicResult,
            apiConnectivity = apiResult,
            recommendation = generateRecommendation(basicResult, apiResult)
        )
        
        Log.d(TAG, "网络诊断完成: $report")
        return report
    }
    
    private fun generateRecommendation(
        basicResult: NetworkTestResult,
        apiResult: NetworkTestResult
    ): String {
        return when {
            basicResult is NetworkTestResult.Failure -> {
                "请检查设备的网络连接（WiFi/移动数据）"
            }
            apiResult is NetworkTestResult.Failure -> {
                when {
                    apiResult.message.contains("代理") -> {
                        "API服务器需要代理访问，请在网络设置中配置代理"
                    }
                    apiResult.message.contains("DNS") -> {
                        "尝试更换DNS为114.114.114.114或8.8.8.8"
                    }
                    else -> {
                        "API服务器暂时不可用，可以尝试使用匿名登录或稍后重试"
                    }
                }
            }
            else -> {
                "网络连接正常，可以正常使用所有功能"
            }
        }
    }
}

sealed class NetworkTestResult {
    data class Success(val message: String) : NetworkTestResult()
    data class Failure(val message: String) : NetworkTestResult()
}

data class NetworkDiagnosisReport(
    val basicConnectivity: NetworkTestResult,
    val apiConnectivity: NetworkTestResult,
    val recommendation: String
)