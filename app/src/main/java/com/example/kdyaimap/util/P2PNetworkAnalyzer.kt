package com.example.kdyaimap.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * P2P网络分析器
 * 用于分析和监控P2P网络连接状态，类似您提供的日志中的Drip协议
 */
@Singleton
class P2PNetworkAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val TAG = "P2PNetworkAnalyzer"
    
    // P2P网络状态
    private val _networkStatus = MutableStateFlow(P2PNetworkStatus.UNKNOWN)
    val networkStatus: StateFlow<P2PNetworkStatus> = _networkStatus.asStateFlow()
    
    // 节点统计信息
    private val _peerStats = MutableStateFlow(PeerStatistics())
    val peerStats: StateFlow<PeerStatistics> = _peerStats.asStateFlow()
    
    // 连接质量指标
    private val _connectionQuality = MutableStateFlow(ConnectionQuality.UNKNOWN)
    val connectionQuality: StateFlow<ConnectionQuality> = _connectionQuality.asStateFlow()
    
    // 错误统计
    private val _errorStats = MutableStateFlow(ErrorStatistics())
    val errorStats: StateFlow<ErrorStatistics> = _errorStats.asStateFlow()
    
    /**
     * 分析P2P日志行
     */
    fun analyzeLogLine(logLine: String) {
        when {
            logLine.contains("total backup peers") -> analyzeBackupPeers(logLine)
            logLine.contains("peer count:") -> analyzePeerCount(logLine)
            logLine.contains("connect timeout") -> analyzeConnectionTimeout(logLine)
            logLine.contains("connect success") -> analyzeConnectionSuccess(logLine)
            logLine.contains("on drip error") -> analyzeDripError(logLine)
            logLine.contains("DELETE") -> analyzePeerDeletion(logLine)
            logLine.contains("CREATE") -> analyzePeerCreation(logLine)
            logLine.contains("Config") -> analyzeConfigUpdate(logLine)
        }
    }
    
    /**
     * 分析备份节点信息
     */
    private fun analyzeBackupPeers(logLine: String) {
        // 解析: total backup peers: 14 (f:0 + b:14 +p:0*0.85) >= expect backup peers: 14 (ppc:10 + r:3)
        val regex = Regex("""total backup peers: (\d+) \(f:(\d+) \+ b:(\d+) \+p:(\d+)\*([\d.]+)\)""")
        val match = regex.find(logLine)
        
        match?.let {
            val total = it.groupValues[1].toInt()
            val formal = it.groupValues[2].toInt()
            val backup = it.groupValues[3].toInt()
            val prepare = it.groupValues[4].toInt()
            val ratio = it.groupValues[5].toDouble()
            
            _peerStats.value = _peerStats.value.copy(
                totalPeers = total,
                formalPeers = formal,
                backupPeers = backup,
                preparePeers = prepare,
                successRatio = ratio
            )
            
            // 更新网络状态
            _networkStatus.value = when {
                total >= 14 -> P2PNetworkStatus.HEALTHY
                total >= 10 -> P2PNetworkStatus.DEGRADED
                else -> P2PNetworkStatus.POOR
            }
        }
    }
    
    /**
     * 分析节点计数
     */
    private fun analyzePeerCount(logLine: String) {
        // 解析: peer count: formal=0, backup=13, prepare=0, unused=8
        val regex = Regex("""peer count: formal=(\d+), backup=(\d+), prepare=(\d+), unused=(\d+)""")
        val match = regex.find(logLine)
        
        match?.let {
            val formal = it.groupValues[1].toInt()
            val backup = it.groupValues[2].toInt()
            val prepare = it.groupValues[3].toInt()
            val unused = it.groupValues[4].toInt()
            
            _peerStats.value = _peerStats.value.copy(
                formalPeers = formal,
                backupPeers = backup,
                preparePeers = prepare,
                unusedPeers = unused
            )
        }
    }
    
    /**
     * 分析连接超时
     */
    private fun analyzeConnectionTimeout(logLine: String) {
        _errorStats.value = _errorStats.value.copy(
            timeoutCount = _errorStats.value.timeoutCount + 1
        )
        
        // 提取peer ID
        val peerIdRegex = Regex("""\[(\w+-[\w.-]+)\]""")
        val peerId = peerIdRegex.find(logLine)?.groupValues?.get(1)
        
        peerId?.let {
            android.util.Log.w(TAG, "P2P连接超时: $it")
        }
        
        updateConnectionQuality()
    }
    
    /**
     * 分析连接成功
     */
    private fun analyzeConnectionSuccess(logLine: String) {
        // 解析连接时间
        val timeRegex = Regex("""connect success, cost time: (\d+)""")
        val match = timeRegex.find(logLine)
        
        match?.let {
            val connectTime = it.groupValues[1].toInt()
            _peerStats.value = _peerStats.value.copy(
                averageConnectTime = (_peerStats.value.averageConnectTime + connectTime) / 2
            )
        }
        
        updateConnectionQuality()
    }
    
    /**
     * 分析Drip错误
     */
    private fun analyzeDripError(logLine: String) {
        // 解析: on drip error:304,status: 3
        val errorRegex = Regex("""on drip error:(\d+),status: (\d+)""")
        val match = errorRegex.find(logLine)
        
        match?.let {
            val errorCode = it.groupValues[1].toInt()
            val status = it.groupValues[2].toInt()
            
            _errorStats.value = _errorStats.value.copy(
                dripErrorCount = _errorStats.value.dripErrorCount + 1,
                lastErrorCode = errorCode,
                lastErrorStatus = status
            )
            
            android.util.Log.e(TAG, "P2P Drip错误: 代码=$errorCode, 状态=$status")
        }
        
        updateConnectionQuality()
    }
    
    /**
     * 分析节点删除
     */
    private fun analyzePeerDeletion(logLine: String) {
        _peerStats.value = _peerStats.value.copy(
            deletedPeers = _peerStats.value.deletedPeers + 1
        )
        
        android.util.Log.i(TAG, "P2P节点已删除")
    }
    
    /**
     * 分析节点创建
     */
    private fun analyzePeerCreation(logLine: String) {
        _peerStats.value = _peerStats.value.copy(
            createdPeers = _peerStats.value.createdPeers + 1
        )
        
        android.util.Log.i(TAG, "P2P节点已创建")
    }
    
    /**
     * 分析配置更新
     */
    private fun analyzeConfigUpdate(logLine: String) {
        if (logLine.contains("request online config")) {
            android.util.Log.i(TAG, "P2P配置更新请求")
        }
        
        // 解析关键配置参数
        when {
            logLine.contains("peer_connect_timeout") -> {
                val timeoutRegex = Regex("""peer_connect_timeout = (\d+)""")
                timeoutRegex.find(logLine)?.let {
                    val timeout = it.groupValues[1].toInt()
                    android.util.Log.i(TAG, "P2P连接超时配置: ${timeout}ms")
                }
            }
            logLine.contains("connect_success_ratio") -> {
                val ratioRegex = Regex("""connect_success_ratio = ([\d.]+)""")
                ratioRegex.find(logLine)?.let {
                    val ratio = it.groupValues[1].toDouble()
                    _peerStats.value = _peerStats.value.copy(successRatio = ratio)
                }
            }
        }
    }
    
    /**
     * 更新连接质量评估
     */
    private fun updateConnectionQuality() {
        val stats = _peerStats.value
        val errors = _errorStats.value
        
        val quality = when {
            stats.backupPeers >= 14 && errors.timeoutCount < 3 -> ConnectionQuality.EXCELLENT
            stats.backupPeers >= 10 && errors.timeoutCount < 5 -> ConnectionQuality.GOOD
            stats.backupPeers >= 7 && errors.timeoutCount < 10 -> ConnectionQuality.FAIR
            else -> ConnectionQuality.POOR
        }
        
        _connectionQuality.value = quality
    }
    
    /**
     * 获取网络诊断报告
     */
    fun getDiagnosticReport(): P2PDiagnosticReport {
        val stats = _peerStats.value
        val errors = _errorStats.value
        val quality = _connectionQuality.value
        
        val recommendations = mutableListOf<String>()
        
        when {
            stats.backupPeers < 10 -> recommendations.add("备份节点数量不足，建议检查网络连接")
            errors.timeoutCount > 5 -> recommendations.add("连接超时频繁，建议增加超时时间或检查网络质量")
            stats.successRatio < 0.8 -> recommendations.add("连接成功率偏低，建议优化网络配置")
            stats.averageConnectTime > 100 -> recommendations.add("连接时间过长，建议选择更近的节点")
        }
        
        return P2PDiagnosticReport(
            networkStatus = _networkStatus.value,
            connectionQuality = quality,
            peerStatistics = stats,
            errorStatistics = errors,
            recommendations = recommendations,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * 重置统计信息
     */
    fun resetStatistics() {
        _peerStats.value = PeerStatistics()
        _errorStats.value = ErrorStatistics()
        _connectionQuality.value = ConnectionQuality.UNKNOWN
        _networkStatus.value = P2PNetworkStatus.UNKNOWN
    }
    
    /**
     * 获取当前网络状态
     */
    fun getNetworkStatus(): NetworkStatusInfo {
        return NetworkStatusInfo(
            isStable = _networkStatus.value == P2PNetworkStatus.HEALTHY,
            hasErrors = _errorStats.value.dripErrorCount > 0 || _errorStats.value.timeoutCount > 0,
            totalPeers = _peerStats.value.totalPeers,
            activePeers = _peerStats.value.formalPeers + _peerStats.value.backupPeers,
            backupPeers = _peerStats.value.backupPeers,
            connectingPeers = _peerStats.value.preparePeers,
            errorCount = _errorStats.value.dripErrorCount + _errorStats.value.timeoutCount,
            criticalErrorCount = _errorStats.value.dripErrorCount,
            warningCount = _errorStats.value.timeoutCount,
            lastErrorTime = if (_errorStats.value.lastErrorCode > 0) System.currentTimeMillis().toString() else ""
        )
    }
}

/**
 * 网络状态信息数据类
 */
data class NetworkStatusInfo(
    val isStable: Boolean,
    val hasErrors: Boolean,
    val totalPeers: Int,
    val activePeers: Int,
    val backupPeers: Int,
    val connectingPeers: Int,
    val errorCount: Int,
    val criticalErrorCount: Int,
    val warningCount: Int,
    val lastErrorTime: String
)

/**
 * P2P网络状态枚举
 */
enum class P2PNetworkStatus {
    UNKNOWN,
    HEALTHY,    // 健康
    DEGRADED,   // 降级
    POOR        // 较差
}

/**
 * 连接质量枚举
 */
enum class ConnectionQuality {
    UNKNOWN,
    EXCELLENT,  // 优秀
    GOOD,       // 良好
    FAIR,       // 一般
    POOR        // 较差
}

/**
 * 节点统计信息
 */
data class PeerStatistics(
    val totalPeers: Int = 0,
    val formalPeers: Int = 0,
    val backupPeers: Int = 0,
    val preparePeers: Int = 0,
    val unusedPeers: Int = 0,
    val createdPeers: Int = 0,
    val deletedPeers: Int = 0,
    val successRatio: Double = 0.0,
    val averageConnectTime: Int = 0
)

/**
 * 错误统计信息
 */
data class ErrorStatistics(
    val timeoutCount: Int = 0,
    val dripErrorCount: Int = 0,
    val lastErrorCode: Int = 0,
    val lastErrorStatus: Int = 0
)

/**
 * P2P诊断报告
 */
data class P2PDiagnosticReport(
    val networkStatus: P2PNetworkStatus,
    val connectionQuality: ConnectionQuality,
    val peerStatistics: PeerStatistics,
    val errorStatistics: ErrorStatistics,
    val recommendations: List<String>,
    val timestamp: Long
)