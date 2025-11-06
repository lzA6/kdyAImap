package com.example.kdyaimap.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kdyaimap.ui.viewmodel.P2PNetworkMonitorViewModel
import com.example.kdyaimap.util.*

/**
 * P2P网络监控界面
 * 用于实时监控P2P网络连接状态和性能指标
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P2PNetworkMonitorScreen(
    onBack: () -> Unit = {},
    viewModel: P2PNetworkMonitorViewModel = hiltViewModel()
) {
    val networkStatus by viewModel.networkStatus.collectAsState()
    val peerStats by viewModel.peerStats.collectAsState()
    val connectionQuality by viewModel.connectionQuality.collectAsState()
    val errorStats by viewModel.errorStats.collectAsState()
    val diagnosticReport by viewModel.diagnosticReport.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("P2P网络监控") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 网络状态概览卡片
            NetworkStatusOverviewCard(
                networkStatus = networkStatus,
                connectionQuality = connectionQuality
            )
            
            // 节点统计卡片
            PeerStatisticsCard(peerStats = peerStats)
            
            // 错误统计卡片
            ErrorStatisticsCard(errorStats = errorStats)
            
            // 实时日志分析卡片
            LogAnalysisCard(
                isAnalyzing = isAnalyzing,
                onAnalyzeLogs = { viewModel.analyzeSampleLogs() }
            )
            
            // 诊断报告卡片
            DiagnosticReportCard(
                report = diagnosticReport,
                onGenerateReport = { viewModel.generateDiagnosticReport() }
            )
            
            // 优化建议卡片
            OptimizationSuggestionsCard(
                networkStatus = networkStatus,
                peerStats = peerStats,
                errorStats = errorStats
            )
        }
    }
}

@Composable
fun NetworkStatusOverviewCard(
    networkStatus: P2PNetworkStatus,
    connectionQuality: ConnectionQuality
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (connectionQuality) {
                ConnectionQuality.EXCELLENT -> Color(0xFF4CAF50)
                ConnectionQuality.GOOD -> Color(0xFF8BC34A)
                ConnectionQuality.FAIR -> Color(0xFFFF9800)
                ConnectionQuality.POOR -> Color(0xFFF44336)
                ConnectionQuality.UNKNOWN -> Color(0xFF9E9E9E)
            }.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "网络状态概览",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "P2P状态",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = when (networkStatus) {
                            P2PNetworkStatus.HEALTHY -> "🟢 健康"
                            P2PNetworkStatus.DEGRADED -> "🟡 降级"
                            P2PNetworkStatus.POOR -> "🔴 较差"
                            P2PNetworkStatus.UNKNOWN -> "⚪ 未知"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column {
                    Text(
                        text = "连接质量",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = when (connectionQuality) {
                            ConnectionQuality.EXCELLENT -> "🟢 优秀"
                            ConnectionQuality.GOOD -> "🟡 良好"
                            ConnectionQuality.FAIR -> "🟠 一般"
                            ConnectionQuality.POOR -> "🔴 较差"
                            ConnectionQuality.UNKNOWN -> "⚪ 未知"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PeerStatisticsCard(peerStats: PeerStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "节点统计",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            // 节点分布
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PeerStatItem("正式节点", peerStats.formalPeers, Color.Blue)
                PeerStatItem("备份节点", peerStats.backupPeers, Color.Green)
                PeerStatItem("准备节点", peerStats.preparePeers, Color(0xFFFF9800))
                PeerStatItem("未使用", peerStats.unusedPeers, Color.Gray)
            }
            
            HorizontalDivider()
            
            // 其他统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("总节点数", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text("${peerStats.totalPeers}", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                
                Column {
                    Text("成功率", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text("${String.format("%.2f", peerStats.successRatio * 100)}%", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                
                Column {
                    Text("平均连接时间", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text("${peerStats.averageConnectTime}ms", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
            
            if (peerStats.createdPeers > 0 || peerStats.deletedPeers > 0) {
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("已创建: ${peerStats.createdPeers}", fontSize = 14.sp)
                    Text("已删除: ${peerStats.deletedPeers}", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun PeerStatItem(label: String, count: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ErrorStatisticsCard(errorStats: ErrorStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (errorStats.timeoutCount > 0 || errorStats.dripErrorCount > 0) 
                MaterialTheme.colorScheme.errorContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "错误统计",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ErrorStatItem("连接超时", errorStats.timeoutCount, Color.Red)
                ErrorStatItem("Drip错误", errorStats.dripErrorCount, Color(0xFFFF9800))
            }
            
            if (errorStats.lastErrorCode > 0) {
                HorizontalDivider()
                Text(
                    text = "最后错误: 代码=${errorStats.lastErrorCode}, 状态=${errorStats.lastErrorStatus}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun ErrorStatItem(label: String, count: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (count > 0) color else Color.Gray
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun LogAnalysisCard(
    isAnalyzing: Boolean,
    onAnalyzeLogs: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "实时日志分析",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "分析P2P网络日志，实时监控连接状态和错误信息",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Button(
                onClick = onAnalyzeLogs,
                enabled = !isAnalyzing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("分析中...")
                } else {
                    Text("分析示例日志")
                }
            }
        }
    }
}

@Composable
fun DiagnosticReportCard(
    report: P2PDiagnosticReport?,
    onGenerateReport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "诊断报告",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = onGenerateReport,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("生成诊断报告")
            }
            
            report?.let {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "网络状态: ${it.networkStatus}",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "连接质量: ${it.connectionQuality}",
                        fontSize = 14.sp
                    )
                    
                    if (it.recommendations.isNotEmpty()) {
                        Text(
                            text = "建议:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        it.recommendations.forEach { recommendation ->
                            Text(
                                text = "• $recommendation",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OptimizationSuggestionsCard(
    networkStatus: P2PNetworkStatus,
    peerStats: PeerStatistics,
    errorStats: ErrorStatistics
) {
    val suggestions = generateOptimizationSuggestions(networkStatus, peerStats, errorStats)
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "优化建议",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            suggestions.forEach { suggestion ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💡",
                        fontSize = 16.sp
                    )
                    Text(
                        text = suggestion,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun generateOptimizationSuggestions(
    networkStatus: P2PNetworkStatus,
    peerStats: PeerStatistics,
    errorStats: ErrorStatistics
): List<String> {
    val suggestions = mutableListOf<String>()
    
    when (networkStatus) {
        P2PNetworkStatus.POOR -> {
            suggestions.add("网络状态较差，建议检查网络连接或重启应用")
            suggestions.add("考虑增加连接超时时间以减少超时错误")
        }
        P2PNetworkStatus.DEGRADED -> {
            suggestions.add("网络状态降级，建议监控连接质量")
            suggestions.add("尝试手动添加更多可靠的节点")
        }
        P2PNetworkStatus.HEALTHY -> {
            suggestions.add("网络状态良好，继续保持当前配置")
        }
        P2PNetworkStatus.UNKNOWN -> {
            suggestions.add("网络状态未知，建议运行诊断以获取更多信息")
        }
    }
    
    if (errorStats.timeoutCount > 5) {
        suggestions.add("连接超时频繁，建议检查网络稳定性或增加超时时间")
    }
    
    if (peerStats.backupPeers < 10) {
        suggestions.add("备份节点数量不足，建议增加节点池大小")
    }
    
    if (peerStats.successRatio < 0.8) {
        suggestions.add("连接成功率偏低，建议优化节点选择算法")
    }
    
    if (peerStats.averageConnectTime > 100) {
        suggestions.add("连接时间较长，建议选择地理位置更近的节点")
    }
    
    return suggestions
}