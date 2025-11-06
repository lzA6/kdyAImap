package com.example.kdyaimap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kdyaimap.util.P2PNetworkAnalyzer
import com.example.kdyaimap.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * P2P网络监控ViewModel
 * 管理P2P网络监控界面的状态和逻辑
 */
@HiltViewModel
class P2PNetworkMonitorViewModel @Inject constructor(
    private val p2pAnalyzer: P2PNetworkAnalyzer
) : ViewModel() {
    
    // 从P2P分析器获取状态流
    val networkStatus = p2pAnalyzer.networkStatus
    val peerStats = p2pAnalyzer.peerStats
    val connectionQuality = p2pAnalyzer.connectionQuality
    val errorStats = p2pAnalyzer.errorStats
    
    // 诊断报告状态
    private val _diagnosticReport = MutableStateFlow<P2PDiagnosticReport?>(null)
    val diagnosticReport: StateFlow<P2PDiagnosticReport?> = _diagnosticReport.asStateFlow()
    
    // 分析状态
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()
    
    /**
     * 分析用户提供的真实日志数据
     * 基于2025-11-06 19:14:01-19:14:03的P2P网络日志
     */
    fun analyzeUserProvidedLogs() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            
            try {
                // 重置统计信息
                p2pAnalyzer.resetStatistics()
                
                // 用户提供的真实日志数据（精选关键日志）
                val userLogs = listOf(
                    "2025-11-06 19:14:01.643 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11] formal=0 backup=15 prepare=0 unused=0, total_peers=15, expect peers = 14\tDripTask.cpp:554",
                    "2025-11-06 19:14:01.645 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] enough peers (formal=0 backup=15 prepare=0 unused=0), skip new tracker request.\tDripTask.cpp:557",
                    "2025-11-06 19:14:02.517 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVD0748790CDB9E.60-68184356][S2C Close] ErrorCode:4\tDripPeer.cpp:691",
                    "2025-11-06 19:14:02.517 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][glbkj-XRVD0748790CDB9E.60-68184356] on drip error:304,status: 3\tDripPeer.cpp:841",
                    "2025-11-06 19:14:02.518 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD0748790CDB9E.60-68184356] close with code: 304\tDripPeer.cpp:892",
                    "2025-11-06 19:14:02.519 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD0748790CDB9E.60-68184356] DELETE\tDripPeer.cpp:81",
                    "2025-11-06 19:14:02.520 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0, backup=14, prepare=0, unused=1\tDripTask.cpp:1524",
                    "2025-11-06 19:14:02.520 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] total backup peers: 14 (f:0 + b:14 +p:0*0.85) >= expect backup peers: 14 (ppc:10 + r:3), skip prepare.\tDripTask.cpp:797",
                    "2025-11-06 19:14:02.523 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD0748790CDB9E.60-68184356] CREATE\tDripPeer.cpp:34",
                    "2025-11-06 19:14:02.524 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVD0748790CDB9E.60-68184356] begin to connect TCP IPv4 peer addr: '112.32.117.201:29801', peer id: , decode optimized 0\tDripPeer.cpp:136",
                    "2025-11-06 19:14:02.572 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD0748790CDB9E.60-68184356] connect success, cost time: 43\tDripPeer.cpp:375",
                    "2025-11-06 19:14:02.572 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD0748790CDB9E.60-68184356] quick request mode, direct on ready.\tDripPeer.cpp:378",
                    "2025-11-06 19:14:02.644 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11] formal=0 backup=15 prepare=0 unused=0, total_peers=15, expect peers = 14\tDripTask.cpp:554",
                    "2025-11-06 19:14:02.644 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] enough peers (formal=0 backup=15 prepare=0 unused=0), skip new tracker request.\tDripTask.cpp:557",
                    "2025-11-06 19:14:02.680 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVD5E764D01F732.1-10027039][S2C Close] ErrorCode:4\tDripPeer.cpp:691",
                    "2025-11-06 19:14:02.680 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][glbkj-XRVD5E764D01F732.1-10027039] on drip error:304,status: 3\tDripPeer.cpp:841",
                    "2025-11-06 19:14:02.681 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD5E764D01F732.1-10027039] DELETE\tDripPeer.cpp:81",
                    "2025-11-06 19:14:02.681 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0, backup=14, prepare=0, unused=1\tDripTask.cpp:1524",
                    "2025-11-06 19:14:02.681 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] total backup peers: 14 (f:0 + b:14 +p:0*0.85) >= expect backup peers: 14 (ppc:10 + r:3), skip prepare.\tDripTask.cpp:797",
                    "2025-11-06 19:14:02.744 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][gldyzlhj-XYBM6932A265440EE9.1-91175181] connect success, cost time: 32\tDripPeer.cpp:375",
                    "2025-11-06 19:14:02.750 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD5E764D01F732.1-10027039] connect success, cost time: 49\tDripPeer.cpp:375",
                    "2025-11-06 19:14:02.761 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVDD83AB36C8797.3-6497545] connect success, cost time: 59\tDripPeer.cpp:375",
                    "2025-11-06 19:14:02.890 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] total backup peers: 14 (f:0 + b:14 +p:0*0.85) >= expect backup peers: 14 (ppc:10 + r:3), skip prepare.\tDripTask.cpp:797",
                    "2025-11-06 19:14:03.024 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD3FCC958C52DC.7-31920987] connect success, cost time: 62\tDripPeer.cpp:375",
                    "2025-11-06 19:14:03.025 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVDBB67107DAF84.2-61920412] connect success, cost time: 60\tDripPeer.cpp:375",
                    "2025-11-06 19:14:03.045 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][gldyzlhj-XYBM661D7A42D196DA.8-2552208] connect success, cost time: 71\tDripPeer.cpp:375",
                    "2025-11-06 19:14:03.160 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] total backup peers: 14 (f:0 + b:14 +p:0*0.85) >= expect backup peers: 14 (ppc:10 + r:3), skip prepare.\tDripTask.cpp:797",
                    "2025-11-06 19:14:03.228 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD89E2CA7CDA2C.26-35910009] connect success, cost time: 47\tDripPeer.cpp:375",
                    "2025-11-06 19:14:03.266 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glzqbwin32-PCWX222E055C94BE.2-8271] connect success, cost time: 86\tDripPeer.cpp:375",
                    "2025-11-06 19:14:03.303 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD4FCE4D2BC839.12-31415971] connect success, cost time: 42\tDripPeer.cpp:375",
                    "2025-11-06 19:14:03.657 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11] formal=0 backup=15 prepare=0 unused=0, total_peers=15, expect peers = 14\tDripTask.cpp:554",
                    "2025-11-06 19:14:03.657 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] enough peers (formal=0 backup=15 prepare=0 unused=0), skip new tracker request.\tDripTask.cpp:557"
                )
                
                // 逐行分析日志
                userLogs.forEach { logLine ->
                    p2pAnalyzer.analyzeLogLine(logLine)
                    kotlinx.coroutines.delay(20)
                }
                
                // 自动生成诊断报告
                generateDiagnosticReport()
                
            } catch (e: Exception) {
                android.util.Log.e("P2PViewModel", "用户日志分析失败", e)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
    /**
     * 分析示例日志
     * 使用您提供的日志示例进行分析
     */
    fun analyzeSampleLogs() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            
            try {
                // 重置统计信息
                p2pAnalyzer.resetStatistics()
                
                // 示例日志数据（基于您提供的日志）
                val sampleLogs = listOf(
                    "2025-11-06 19:13:39.473 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] total backup peers: 14 (f:0 + b:14 +p:0*0.85) >= expect backup peers: 14 (ppc:10 + r:3), skip prepare.\tDripTask.cpp:797",
                    "2025-11-06 19:13:39.474 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11] formal=0 backup=14 prepare=0 unused=7, total_peers=19, expect peers = 14\tDripTask.cpp:554",
                    "2025-11-06 19:13:39.588 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkjtfdl-XYBMEF1EB915846430.6-72921543][S2C Close] ErrorCode:4\tDripPeer.cpp:691",
                    "2025-11-06 19:13:39.589 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][glbkjtfdl-XYBMEF1EB915846430.6-72921543] on drip error:304,status: 3\tDripPeer.cpp:841",
                    "2025-11-06 19:13:39.590 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkjtfdl-XYBMEF1EB915846430.6-72921543] close with code: 304\tDripPeer.cpp:892",
                    "2025-11-06 19:13:39.590 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkjtfdl-XYBMEF1EB915846430.6-72921543] DELETE\tDripPeer.cpp:81",
                    "2025-11-06 19:13:39.590 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0, backup=13, prepare=0, unused=8\tDripTask.cpp:1524",
                    "2025-11-06 19:13:39.592 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glys-8574821c545ee46a8b61bf9dc444f52c-8371] CREATE\tDripPeer.cpp:34",
                    "2025-11-06 19:13:39.592 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glhw-STWA039DC7758C81-90110795] CREATE\tDripPeer.cpp:34",
                    "2025-11-06 19:13:39.891 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][glys-8574821c545ee46a8b61bf9dc444f52c-8371] connect timeout.\tDripPeer.cpp:401",
                    "2025-11-06 19:13:39.891 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][glys-8574821c545ee46a8b61bf9dc444f52c-8371] on drip error:1,status: 1\tDripPeer.cpp:841",
                    "2025-11-06 19:13:39.892 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glys-8574821c545ee46a8b61bf9dc444f52c-8371] close with code: 1\tDripPeer.cpp:892",
                    "2025-11-06 19:13:39.893 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glys-8574821c545ee46a8b61bf9dc444f52c-8371] DELETE\tDripPeer.cpp:81",
                    "2025-11-06 19:13:39.896 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][glhw-STWA039DC7758C81-90110795] connect timeout.\tDripPeer.cpp:401",
                    "2025-11-06 19:13:39.896 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][glhw-STWA039DC7758C81-90110795] on drip error:1,status: 1\tDripPeer.cpp:841",
                    "2025-11-06 19:13:39.897 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glhw-STWA039DC7758C81-90110795] close with code: 1\tDripPeer.cpp:892",
                    "2025-11-06 19:13:39.898 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glhw-STWA039DC7758C81-90110795] DELETE\tDripPeer.cpp:81",
                    "2025-11-06 19:13:39.960 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVDA3022F8CF38D.15-74029390] connect success, cost time: 58\tDripPeer.cpp:375",
                    "2025-11-06 19:13:39.960 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVDA3022F8CF38D.15-74029390] quick request mode, direct on ready.\tDripPeer.cpp:378",
                    "2025-11-06 19:13:40.198 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][glys-3068421cd962ccb1a65dc6e8557e5390-1210] connect timeout.\tDripPeer.cpp:401",
                    "2025-11-06 19:13:40.198 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][glys-3068421cd962ccb1a65dc6e8557e5390-1210] on drip error:1,status: 1\tDripPeer.cpp:841",
                    "2025-11-06 19:13:40.199 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glys-3068421cd962ccb1a65dc6e8557e5390-1210] close with code: 1\tDripPeer.cpp:892",
                    "2025-11-06 19:13:40.199 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glys-3068421cd962ccb1a65dc6e8557e5390-1210] DELETE\tDripPeer.cpp:81",
                    "2025-11-06 19:13:41.518 28302-7192  xySDK                   usap64                               D  [KsConfig][Drip] connect_success_ratio = 0.85\tDripConfig.cpp:48",
                    "2025-11-06 19:13:41.518 28302-7192  xySDK                   usap64                               D  [KsConfig][Drip] peer_connect_timeout = 300\tDripConfig.cpp:76"
                )
                
                // 逐行分析日志
                sampleLogs.forEach { logLine ->
                    p2pAnalyzer.analyzeLogLine(logLine)
                    // 添加小延迟以模拟实时分析
                    kotlinx.coroutines.delay(50)
                }
                
            } catch (e: Exception) {
                android.util.Log.e("P2PViewModel", "日志分析失败", e)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
    
    /**
     * 生成诊断报告
     */
    fun generateDiagnosticReport() {
        viewModelScope.launch {
            try {
                val report = p2pAnalyzer.getDiagnosticReport()
                _diagnosticReport.value = report
            } catch (e: Exception) {
                android.util.Log.e("P2PViewModel", "生成诊断报告失败", e)
            }
        }
    }
    
    /**
     * 重置所有统计数据
     */
    fun resetStatistics() {
        viewModelScope.launch {
            p2pAnalyzer.resetStatistics()
            _diagnosticReport.value = null
        }
    }
    
    /**
     * 分析自定义日志文本
     */
    fun analyzeCustomLogs(logText: String) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            
            try {
                val logLines = logText.split("\n")
                logLines.forEach { logLine ->
                    if (logLine.trim().isNotEmpty()) {
                        p2pAnalyzer.analyzeLogLine(logLine)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("P2PViewModel", "自定义日志分析失败", e)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
    
    /**
     * 获取当前网络状态的详细描述
     */
    fun getNetworkStatusDescription(): String {
        val status = networkStatus.value
        val quality = connectionQuality.value
        val stats = peerStats.value
        val errors = errorStats.value
        
        return buildString {
            appendLine("=== P2P网络状态报告 ===")
            appendLine("网络状态: $status")
            appendLine("连接质量: $quality")
            appendLine()
            appendLine("=== 节点统计 ===")
            appendLine("总节点数: ${stats.totalPeers}")
            appendLine("正式节点: ${stats.formalPeers}")
            appendLine("备份节点: ${stats.backupPeers}")
            appendLine("准备节点: ${stats.preparePeers}")
            appendLine("未使用节点: ${stats.unusedPeers}")
            appendLine("成功率: ${String.format("%.2f", stats.successRatio * 100)}%")
            appendLine("平均连接时间: ${stats.averageConnectTime}ms")
            appendLine()
            appendLine("=== 错误统计 ===")
            appendLine("连接超时: ${errors.timeoutCount}")
            appendLine("Drip错误: ${errors.dripErrorCount}")
            if (errors.lastErrorCode > 0) {
                appendLine("最后错误代码: ${errors.lastErrorCode}")
                appendLine("最后错误状态: ${errors.lastErrorStatus}")
            }
        }
    }
}