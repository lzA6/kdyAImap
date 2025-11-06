package com.example.kdyaimap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kdyaimap.ui.viewmodel.P2PNetworkMonitorViewModelClean
import com.example.kdyaimap.util.LogAnalysisResultClean

/**
 * P2P网络监控界面 - 清理版本
 * 避免函数重复定义，专注于用户日志分析展示
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P2PNetworkMonitorScreenClean(
    onBack: () -> Unit,
    viewModel: P2PNetworkMonitorViewModelClean = hiltViewModel()
) {
    val networkStatus by viewModel.networkStatus.collectAsStateWithLifecycle()
    val peerStats by viewModel.peerStats.collectAsStateWithLifecycle()
    val errorStats by viewModel.errorStats.collectAsStateWithLifecycle()
    val connectionStats by viewModel.connectionStats.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val analysisResult: LogAnalysisResultClean? by viewModel.analysisResult.collectAsStateWithLifecycle()
    val diagnosticReport by viewModel.diagnosticReport.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("P2P网络监控") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
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
            // 网络状态卡片
            NetworkStatusCardClean(networkStatus)
            
            // 统计信息卡片
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PeerStatsCardClean(
                    peerStats = peerStats,
                    modifier = Modifier.weight(1f)
                )
                ErrorStatsCardClean(
                    errorStats = errorStats,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 连接统计卡片
            ConnectionStatsCardClean(connectionStats)
            
            // 用户日志分析按钮
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "日志分析",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { viewModel.analyzeUserProvidedLogs() },
                        enabled = !isAnalyzing
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("分析中...")
                        } else {
                            Icon(Icons.Default.Analytics, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("分析用户日志")
                        }
                    }
                }
            }
            
            // 分析结果展示
            analysisResult?.let { result ->
                AnalysisResultCardClean(result)
            }
            
            // 诊断报告
            diagnosticReport?.let { report ->
                DiagnosticReportCardClean(report)
            }
            
            // 优化建议
            diagnosticReport?.let { report ->
                OptimizationSuggestionsCardClean(report)
            }
            
            // 优化操作按钮
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "网络优化",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { viewModel.applyOptimizations() }
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("应用优化建议")
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkStatusCardClean(status: com.example.kdyaimap.ui.viewmodel.P2PNetworkStatusClean) {
    val (statusText, statusColor) = when (status) {
        com.example.kdyaimap.ui.viewmodel.P2PNetworkStatusClean.STABLE -> "稳定" to Color.Green
        com.example.kdyaimap.ui.viewmodel.P2PNetworkStatusClean.CONNECTING -> "连接中" to Color.Blue
        com.example.kdyaimap.ui.viewmodel.P2PNetworkStatusClean.UNSTABLE -> "不稳定" to Color(0xFFFF9800)
        com.example.kdyaimap.ui.viewmodel.P2PNetworkStatusClean.WARNING -> "警告" to Color(0xFFFF9800)
        com.example.kdyaimap.ui.viewmodel.P2PNetworkStatusClean.CRITICAL -> "严重" to Color.Red
        com.example.kdyaimap.ui.viewmodel.P2PNetworkStatusClean.ERROR -> "错误" to Color.Red
        com.example.kdyaimap.ui.viewmodel.P2PNetworkStatusClean.UNKNOWN -> "未知" to Color.Gray
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, shape = MaterialTheme.shapes.small)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "网络状态",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun PeerStatsCardClean(peerStats: com.example.kdyaimap.ui.viewmodel.PeerStatisticsClean, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "节点统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            StatItemClean("总节点", peerStats.totalPeers.toString())
            StatItemClean("活跃节点", peerStats.activePeers.toString())
            StatItemClean("备份节点", peerStats.backupPeers.toString())
            StatItemClean("连接中", peerStats.connectingPeers.toString())
        }
    }
}

@Composable
private fun ErrorStatsCardClean(errorStats: com.example.kdyaimap.ui.viewmodel.ErrorStatisticsClean, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "错误统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            StatItemClean("总错误", errorStats.totalErrors.toString(), Color.Red)
            StatItemClean("严重错误", errorStats.criticalErrors.toString(), Color.Red)
            StatItemClean("警告", errorStats.warnings.toString(), Color(0xFFFF9800))
            
            if (errorStats.lastErrorTime.isNotEmpty()) {
                StatItemClean("最后错误", errorStats.lastErrorTime)
            }
        }
    }
}

@Composable
private fun ConnectionStatsCardClean(connectionStats: com.example.kdyaimap.ui.viewmodel.ConnectionStatisticsClean) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "连接统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItemClean("总连接", connectionStats.totalConnections.toString())
                StatItemClean("成功", connectionStats.successfulConnections.toString(), Color.Green)
                StatItemClean("失败", connectionStats.failedConnections.toString(), Color.Red)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            StatItemClean(
                "平均连接时间",
                "${connectionStats.averageConnectionTime.toInt()}ms"
            )
        }
    }
}

@Composable
private fun AnalysisResultCardClean(result: com.example.kdyaimap.util.LogAnalysisResultClean) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "日志分析结果",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItemClean("总事件", result.summary.totalEvents.toString())
                StatItemClean("总错误", result.summary.totalErrors.toString())
                StatItemClean("健康评分", "${result.summary.healthScore}%", 
                    if (result.summary.healthScore > 70) Color.Green else Color(0xFFFF9800))
            }
            
            // 性能指标
            if (result.metrics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "性能指标",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                result.metrics.forEach { metric ->
                    StatItemClean(
                        metric.name,
                        "${metric.value.toInt()}${metric.unit}"
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticReportCardClean(report: com.example.kdyaimap.ui.viewmodel.P2PDiagnosticReportClean) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "诊断报告",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            StatItemClean("健康评分", "${report.overallHealth}%")
            StatItemClean("节点分析", report.peerAnalysis)
            StatItemClean("错误分析", report.errorAnalysis)
            
            if (report.keyIssues.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "关键问题",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.Red
                )
                
                report.keyIssues.forEach { issue ->
                    Text(
                        text = "• $issue",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OptimizationSuggestionsCardClean(report: com.example.kdyaimap.ui.viewmodel.P2PDiagnosticReportClean) {
    if (report.recommendations.isEmpty()) return
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "优化建议",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            report.recommendations.forEachIndexed { index, suggestion ->
                Text(
                    text = "${index + 1}. $suggestion",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun StatItemClean(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}