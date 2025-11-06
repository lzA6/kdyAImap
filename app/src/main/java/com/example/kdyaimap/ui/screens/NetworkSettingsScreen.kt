package com.example.kdyaimap.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kdyaimap.core.data.network.NetworkStatus
import com.example.kdyaimap.ui.viewmodel.NetworkSettingsViewModel
import com.example.kdyaimap.util.NetworkDiagnosisReport
import com.example.kdyaimap.util.NetworkTestResult
import androidx.compose.runtime.getValue
import kotlinx.coroutines.launch

/**
 * 网络设置界面
 * 用于配置网络连接和测试连接状态
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSettingsScreen(
    onBack: () -> Unit = {},
    viewModel: NetworkSettingsViewModel = hiltViewModel()
) {
    val networkStatus by viewModel.networkStatus.collectAsState()
    val isTesting by viewModel.isTesting.collectAsState()
    val proxyEnabled by viewModel.proxyEnabled.collectAsState()
    val proxyHost by viewModel.proxyHost.collectAsState()
    val proxyPort by viewModel.proxyPort.collectAsState()
    val diagnosisReport by viewModel.diagnosisReport.collectAsState()
    val isDiagnosing by viewModel.isDiagnosing.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("网络设置") },
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
            // 网络状态卡片
            NetworkStatusCard(
                status = networkStatus,
                isTesting = isTesting,
                onTestConnection = { viewModel.testConnection() }
            )
            
            // 代理设置卡片
            ProxySettingsCard(
                proxyEnabled = proxyEnabled,
                proxyHost = proxyHost,
                proxyPort = proxyPort,
                onProxyEnabledChange = { viewModel.updateProxyEnabled(it) },
                onProxyHostChange = { viewModel.updateProxyHost(it) },
                onProxyPortChange = { viewModel.updateProxyPort(it) }
            )
            
            // 网络诊断卡片
            NetworkDiagnosisCard(
                diagnosisReport = diagnosisReport,
                isDiagnosing = isDiagnosing,
                onRunDiagnosis = { viewModel.runNetworkDiagnosis() }
            )
            
            // 网络解决方案说明
            NetworkSolutionsCard()
        }
    }
}

@Composable
fun NetworkStatusCard(
    status: NetworkStatus,
    isTesting: Boolean,
    onTestConnection: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                NetworkStatus.CONNECTED -> MaterialTheme.colorScheme.primaryContainer
                NetworkStatus.FAILOVER_MODE -> MaterialTheme.colorScheme.secondaryContainer
                NetworkStatus.DISCONNECTED -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "网络连接状态",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = when (status) {
                    NetworkStatus.CONNECTED -> "✅ 连接正常"
                    NetworkStatus.FAILOVER_MODE -> "⚠️ 故障转移模式（使用模拟数据）"
                    NetworkStatus.DISCONNECTED -> "❌ 连接失败"
                },
                fontSize = 16.sp
            )
            
            Text(
                text = when (status) {
                    NetworkStatus.CONNECTED -> "可以正常访问Cloudflare Workers API"
                    NetworkStatus.FAILOVER_MODE -> "主API不可用，使用本地模拟数据"
                    NetworkStatus.DISCONNECTED -> "无法连接到服务器，请检查网络或代理设置"
                },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Button(
                onClick = onTestConnection,
                enabled = !isTesting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("测试中...")
                } else {
                    Text("测试连接")
                }
            }
        }
    }
}

@Composable
fun ProxySettingsCard(
    proxyEnabled: Boolean,
    proxyHost: String,
    proxyPort: String,
    onProxyEnabledChange: (Boolean) -> Unit,
    onProxyHostChange: (String) -> Unit,
    onProxyPortChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "代理设置",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = proxyEnabled,
                    onCheckedChange = onProxyEnabledChange
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("启用代理")
            }
            
            if (proxyEnabled) {
                OutlinedTextField(
                    value = proxyHost,
                    onValueChange = onProxyHostChange,
                    label = { Text("代理服务器地址") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("例如：127.0.0.1") }
                )
                
                OutlinedTextField(
                    value = proxyPort,
                    onValueChange = onProxyPortChange,
                    label = { Text("代理端口") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("例如：7890") }
                )
            }
        }
    }
}

@Composable
fun NetworkSolutionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "网络解决方案",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "如果遇到网络连接问题，可以尝试以下解决方案：",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Text("1. 🔄 启用代理：配置VPN或代理服务器", fontSize = 13.sp)
            Text("2. 📱 使用移动网络：切换到4G/5G网络", fontSize = 13.sp)
            Text("3. 🌐 更换DNS：使用114.114.114.114或8.8.8.8", fontSize = 13.sp)
            Text("4. 🔄 重启应用：清理缓存后重新启动", fontSize = 13.sp)
            Text("5. 📡 故障转移：应用会自动使用模拟数据", fontSize = 13.sp)
            
            Text(
                text = "注意：Cloudflare Workers在国内可能需要代理访问",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun NetworkDiagnosisCard(
    diagnosisReport: NetworkDiagnosisReport?,
    isDiagnosing: Boolean,
    onRunDiagnosis: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "网络诊断",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = onRunDiagnosis,
                enabled = !isDiagnosing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isDiagnosing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("诊断中...")
                } else {
                    Text("运行网络诊断")
                }
            }
            
            diagnosisReport?.let { report ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "基础网络连接: ${
                            when (val result = report.basicConnectivity) {
                                is NetworkTestResult.Success -> "✅ ${result.message}"
                                is NetworkTestResult.Failure -> "❌ ${result.message}"
                            }
                        }",
                        fontSize = 14.sp
                    )
                    
                    Text(
                        text = "API服务器连接: ${
                            when (val result = report.apiConnectivity) {
                                is NetworkTestResult.Success -> "✅ ${result.message}"
                                is NetworkTestResult.Failure -> "❌ ${result.message}"
                            }
                        }",
                        fontSize = 14.sp
                    )
                    
                    Text(
                        text = "建议: ${report.recommendation}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}