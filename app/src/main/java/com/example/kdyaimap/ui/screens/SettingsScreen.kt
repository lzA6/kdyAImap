package com.example.kdyaimap.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kdyaimap.ui.viewmodel.SettingsViewModel
import com.example.kdyaimap.ui.viewmodel.SettingsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val settingsState by settingsViewModel.settingsState.collectAsState()
    
    LaunchedEffect(Unit) {
        settingsViewModel.loadSettings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = settingsState) {
            is SettingsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is SettingsState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 账户设置
                    item {
                        SettingsSection(title = "账户设置") {
                            SettingsItem(
                                title = "个人资料",
                                subtitle = "编辑你的基本信息",
                                icon = Icons.Default.Person,
                                onClick = { /* 导航到编辑资料 */ }
                            )
                            
                            SettingsItem(
                                title = "账号安全",
                                subtitle = "密码、登录验证",
                                icon = Icons.Default.Security,
                                onClick = { /* 导航到账号安全 */ }
                            )
                            
                            SettingsItem(
                                title = "隐私设置",
                                subtitle = "控制你的信息可见性",
                                icon = Icons.Default.Visibility,
                                onClick = { /* 导航到隐私设置 */ }
                            )
                        }
                    }
                    
                    // 通知设置
                    item {
                        SettingsSection(title = "通知设置") {
                            SettingsSwitchItem(
                                title = "活动提醒",
                                subtitle = "新活动发布时通知我",
                                icon = Icons.Default.Notifications,
                                checked = state.settings.eventNotifications,
                                onCheckedChange = { enabled ->
                                    settingsViewModel.updateEventNotifications(enabled)
                                }
                            )
                            
                            SettingsSwitchItem(
                                title = "消息通知",
                                subtitle = "收到新消息时通知我",
                                icon = Icons.AutoMirrored.Filled.Message,
                                checked = state.settings.messageNotifications,
                                onCheckedChange = { enabled ->
                                    settingsViewModel.updateMessageNotifications(enabled)
                                }
                            )
                            
                            SettingsSwitchItem(
                                title = "系统通知",
                                subtitle = "系统更新和重要通知",
                                icon = Icons.Default.SystemUpdate,
                                checked = state.settings.systemNotifications,
                                onCheckedChange = { enabled ->
                                    settingsViewModel.updateSystemNotifications(enabled)
                                }
                            )
                        }
                    }
                    
                    // 地图设置
                    item {
                        SettingsSection(title = "地图设置") {
                            SettingsSwitchItem(
                                title = "自动定位",
                                subtitle = "自动获取当前位置",
                                icon = Icons.Default.LocationOn,
                                checked = state.settings.autoLocation,
                                onCheckedChange = { enabled ->
                                    settingsViewModel.updateAutoLocation(enabled)
                                }
                            )
                            
                            SettingsSwitchItem(
                                title = "显示导航路线",
                                subtitle = "在地图上显示导航路线",
                                icon = Icons.Default.Directions,
                                checked = state.settings.showNavigation,
                                onCheckedChange = { enabled ->
                                    settingsViewModel.updateShowNavigation(enabled)
                                }
                            )
                            
                            SettingsItem(
                                title = "地图样式",
                                subtitle = "选择地图显示样式",
                                icon = Icons.Default.Map,
                                onClick = { /* 显示地图样式选择 */ }
                            )
                        }
                    }
                    
                    // 其他设置
                    item {
                        SettingsSection(title = "其他") {
                            SettingsItem(
                                title = "清除缓存",
                                subtitle = "清理应用缓存数据",
                                icon = Icons.Default.CleaningServices,
                                onClick = { settingsViewModel.clearCache() }
                            )
                            
                            SettingsItem(
                                title = "关于我们",
                                subtitle = "版本信息和帮助",
                                icon = Icons.Default.Info,
                                onClick = { /* 显示关于页面 */ }
                            )
                            
                            SettingsItem(
                                title = "意见反馈",
                                subtitle = "告诉我们你的想法",
                                icon = Icons.Default.Feedback,
                                onClick = { /* 打开反馈页面 */ }
                            )
                        }
                    }
                    
                    // 退出登录
                    item {
                        Button(
                            onClick = { settingsViewModel.logout() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("退出登录")
                        }
                    }
                }
            }
            
            is SettingsState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { settingsViewModel.loadSettings() }) {
                            Text("重试")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}