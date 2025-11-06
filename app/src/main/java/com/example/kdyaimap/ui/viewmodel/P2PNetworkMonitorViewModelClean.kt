package com.example.kdyaimap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kdyaimap.util.P2PLogAnalyzerClean
import com.example.kdyaimap.util.P2PNetworkAnalyzer
import com.example.kdyaimap.util.P2PNetworkOptimizer
import com.example.kdyaimap.util.LogAnalysisResultClean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * P2P网络监控ViewModel - 清理版本
 * 避免依赖冲突，专注于用户日志分析
 */
@HiltViewModel
class P2PNetworkMonitorViewModelClean @Inject constructor(
    private val p2pAnalyzer: P2PNetworkAnalyzer,
    private val p2pLogAnalyzer: P2PLogAnalyzerClean,
    private val p2pOptimizer: P2PNetworkOptimizer
) : ViewModel() {
    
    private val _networkStatus = MutableStateFlow(P2PNetworkStatusClean.UNKNOWN)
    val networkStatus: StateFlow<P2PNetworkStatusClean> = _networkStatus.asStateFlow()
    
    private val _peerStats = MutableStateFlow(PeerStatisticsClean())
    val peerStats: StateFlow<PeerStatisticsClean> = _peerStats.asStateFlow()
    
    private val _errorStats = MutableStateFlow(ErrorStatisticsClean())
    val errorStats: StateFlow<ErrorStatisticsClean> = _errorStats.asStateFlow()
    
    private val _connectionStats = MutableStateFlow(ConnectionStatisticsClean())
    val connectionStats: StateFlow<ConnectionStatisticsClean> = _connectionStats.asStateFlow()
    
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()
    
    private val _analysisResult = MutableStateFlow<LogAnalysisResultClean?>(null)
    val analysisResult: StateFlow<LogAnalysisResultClean?> = _analysisResult.asStateFlow()
    
    private val _diagnosticReport = MutableStateFlow<P2PDiagnosticReportClean?>(null)
    val diagnosticReport: StateFlow<P2PDiagnosticReportClean?> = _diagnosticReport.asStateFlow()
    
    init {
        analyzeNetworkStatus()
    }
    
    /**
     * 分析用户提供的日志
     */
    fun analyzeUserProvidedLogs() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val result = p2pLogAnalyzer.analyzeUserLogs()
                _analysisResult.value = result
                
                // 更新统计信息
                updateStatisticsFromAnalysis(result)
                
                // 生成诊断报告
                generateDiagnosticReport(result)
                
            } catch (e: Exception) {
                // 处理错误
                _networkStatus.value = P2PNetworkStatusClean.ERROR
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
    
    /**
     * 分析网络状态
     */
    private fun analyzeNetworkStatus() {
        viewModelScope.launch {
            try {
                val status = p2pAnalyzer.getNetworkStatus()
                _networkStatus.value = when {
                    status.isStable -> P2PNetworkStatusClean.STABLE
                    status.hasErrors -> P2PNetworkStatusClean.UNSTABLE
                    else -> P2PNetworkStatusClean.CONNECTING
                }
                
                _peerStats.value = PeerStatisticsClean(
                    totalPeers = status.totalPeers,
                    activePeers = status.activePeers,
                    backupPeers = status.backupPeers,
                    connectingPeers = status.connectingPeers
                )
                
                _errorStats.value = ErrorStatisticsClean(
                    totalErrors = status.errorCount,
                    criticalErrors = status.criticalErrorCount,
                    warnings = status.warningCount,
                    lastErrorTime = status.lastErrorTime
                )
                
            } catch (e: Exception) {
                _networkStatus.value = P2PNetworkStatusClean.ERROR
            }
        }
    }
    
    /**
     * 从分析结果更新统计信息
     */
    private fun updateStatisticsFromAnalysis(result: LogAnalysisResultClean) {
        val dripErrors = result.errors.count { it.type.contains("DRIP_ERROR") }
        val connectionSuccess = result.events.count { it.type.contains("CONNECTION_SUCCESS") }
        val connectionFailures = result.events.count { it.type.contains("CONNECTION_FAILURE") }
        
        _errorStats.value = ErrorStatisticsClean(
            totalErrors = result.errors.size,
            criticalErrors = dripErrors,
            warnings = 0,
            lastErrorTime = result.errors.lastOrNull()?.timestamp ?: ""
        )
        
        _connectionStats.value = ConnectionStatisticsClean(
            totalConnections = connectionSuccess + connectionFailures,
            successfulConnections = connectionSuccess,
            failedConnections = connectionFailures,
            averageConnectionTime = result.metrics.find { it.name == "平均连接时间" }?.value ?: 0.0
        )
        
        // 更新网络状态
        _networkStatus.value = when {
            dripErrors > 10 -> P2PNetworkStatusClean.CRITICAL
            dripErrors > 5 -> P2PNetworkStatusClean.UNSTABLE
            dripErrors > 0 -> P2PNetworkStatusClean.WARNING
            else -> P2PNetworkStatusClean.STABLE
        }
    }
    
    /**
     * 生成诊断报告
     */
    private fun generateDiagnosticReport(result: LogAnalysisResultClean) {
        val report = P2PDiagnosticReportClean(
            timestamp = System.currentTimeMillis(),
            overallHealth = result.summary.healthScore,
            networkStatus = _networkStatus.value,
            keyIssues = identifyKeyIssues(result),
            recommendations = generateRecommendations(result),
            peerAnalysis = analyzePeerBehavior(result),
            errorAnalysis = analyzeErrorPatterns(result)
        )
        
        _diagnosticReport.value = report
    }
    
    /**
     * 识别关键问题
     */
    private fun identifyKeyIssues(result: LogAnalysisResultClean): List<String> {
        val issues = mutableListOf<String>()
        
        if (result.summary.criticalErrors > 0) {
            issues.add("检测到${result.summary.criticalErrors}个严重P2P连接错误")
        }
        
        val dripErrors = result.errors.count { it.type.contains("DRIP_ERROR") }
        if (dripErrors > 10) {
            issues.add("P2P连接错误率过高，可能影响网络稳定性")
        }
        
        val avgConnectionTime = result.metrics.find { it.name == "平均连接时间" }?.value ?: 0.0
        if (avgConnectionTime > 100) {
            issues.add("平均连接时间过长(${avgConnectionTime.toInt()}ms)，建议优化网络配置")
        }
        
        return issues
    }
    
    /**
     * 生成优化建议
     */
    private fun generateRecommendations(result: LogAnalysisResultClean): List<String> {
        val recommendations = mutableListOf<String>()
        
        val dripErrors = result.errors.count { it.type.contains("DRIP_ERROR") }
        if (dripErrors > 0) {
            recommendations.add("实施智能重连机制，减少304错误的影响")
            recommendations.add("增加连接超时时间，提高连接成功率")
        }
        
        val avgConnectionTime = result.metrics.find { it.name == "平均连接时间" }?.value ?: 0.0
        if (avgConnectionTime > 50) {
            recommendations.add("优化DNS解析和网络路由，减少连接延迟")
        }
        
        recommendations.add("启用连接池管理，提高资源利用效率")
        recommendations.add("实施实时监控和告警机制")
        
        return recommendations
    }
    
    /**
     * 分析对等节点行为
     */
    private fun analyzePeerBehavior(result: LogAnalysisResultClean): String {
        val createEvents = result.events.count { it.type.contains("PEER_CREATE") }
        val deleteEvents = result.events.count { it.type.contains("PEER_DELETE") }
        val successEvents = result.events.count { it.type.contains("CONNECTION_SUCCESS") }
        
        return "节点创建: $createEvents, 节点删除: $deleteEvents, 成功连接: $successEvents"
    }
    
    /**
     * 分析错误模式
     */
    private fun analyzeErrorPatterns(result: LogAnalysisResultClean): String {
        val dripErrors = result.errors.count { it.type.contains("DRIP_ERROR") }
        val totalErrors = result.errors.size
        
        return "总错误: $totalErrors, DRIP错误: $dripErrors (${if (totalErrors > 0) (dripErrors * 100 / totalErrors) else 0}%)"
    }
    
    /**
     * 应用优化建议
     */
    fun applyOptimizations() {
        viewModelScope.launch {
            try {
                // 应用优化建议
                val suggestions = p2pOptimizer.optimizationSuggestions.value
                if (suggestions.isNotEmpty()) {
                    p2pOptimizer.applyOptimization(suggestions.first())
                }
                
                // 重新分析状态
                analyzeNetworkStatus()
                
            } catch (e: Exception) {
                _networkStatus.value = P2PNetworkStatusClean.ERROR
            }
        }
    }
    
    /**
     * 刷新状态
     */
    fun refresh() {
        analyzeNetworkStatus()
    }
}

// 数据类定义 - 避免重复定义
enum class P2PNetworkStatusClean {
    UNKNOWN,
    STABLE,
    CONNECTING,
    UNSTABLE,
    WARNING,
    CRITICAL,
    ERROR
}

data class PeerStatisticsClean(
    val totalPeers: Int = 0,
    val activePeers: Int = 0,
    val backupPeers: Int = 0,
    val connectingPeers: Int = 0
)

data class ErrorStatisticsClean(
    val totalErrors: Int = 0,
    val criticalErrors: Int = 0,
    val warnings: Int = 0,
    val lastErrorTime: String = ""
)

data class ConnectionStatisticsClean(
    val totalConnections: Int = 0,
    val successfulConnections: Int = 0,
    val failedConnections: Int = 0,
    val averageConnectionTime: Double = 0.0
)

data class P2PDiagnosticReportClean(
    val timestamp: Long,
    val overallHealth: Int,
    val networkStatus: P2PNetworkStatusClean,
    val keyIssues: List<String>,
    val recommendations: List<String>,
    val peerAnalysis: String,
    val errorAnalysis: String
)