package com.example.kdyaimap.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.example.kdyaimap.core.model.MapTag
import com.example.kdyaimap.core.model.TaggedLocation
import com.example.kdyaimap.ui.viewmodel.MapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // 权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val hasFineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val hasCoarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val hasLocationPermission = hasFineLocation || hasCoarseLocation
        
        if (hasLocationPermission) {
            viewModel.startLocation()
        } else {
            viewModel.showErrorMessage("定位权限被拒绝，请在设置中开启定位权限以使用地图功能")
        }
    }
    
    LaunchedEffect(Unit) {
        // 检查权限并启动定位
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasFineLocation && hasCoarseLocation) {
            viewModel.startLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    // 监听位置数据变化，更新地图标记
    LaunchedEffect(uiState.locations) {
        // 清除现有标记并添加新标记
        // 这里需要通过ViewModel来管理标记的添加和删除
    }
    
    // 设置地图点击监听器
    LaunchedEffect(Unit) {
        viewModel.setMapClickListener { latLng: LatLng ->
            viewModel.showLocationDialog(latLng)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 地图视图
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    viewModel.initializeMap(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // 顶部工具栏
        TopAppBar(
            modifier = Modifier.align(Alignment.TopCenter),
            title = { Text("地图标签") },
            actions = {
                IconButton(onClick = { viewModel.showTagDialog() }) {
                    Icon(Icons.Default.Add, "添加标签")
                }
                IconButton(onClick = { 
                    // 清除所有导航标记
                    viewModel.clearNavigationMarkers()
                    viewModel.showSuccessMessage("已清除导航标记")
                }) {
                    Icon(
                        Icons.Default.Clear,
                        "清除标记",
                        tint = Color(0xFFFF9800) // Orange color
                    )
                }
                IconButton(onClick = { viewModel.startLocation() }) {
                    Icon(
                        Icons.Default.MyLocation,
                        "定位",
                        tint = if (uiState.isLocating) Color.Blue else Color.Gray
                    )
                }
            }
        )
        
        // 浮动操作按钮 - 快速添加当前位置标记
        if (uiState.currentLocation != null) {
            FloatingActionButton(
                onClick = { 
                    viewModel.showLocationDialog(uiState.currentLocation!!)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .offset(y = (-220).dp), // 避免与底部标签列表重叠
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.AddLocation,
                    "添加位置标记",
                    tint = Color.White
                )
            }
        }
        
        // 底部标签列表
        if (uiState.tags.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "标签列表",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.tags) { tag ->
                            TagItem(
                                tag = tag,
                                isSelected = selectedTag?.id == tag.id,
                                onClick = { viewModel.selectTag(if (selectedTag?.id == tag.id) null else tag) },
                                onDelete = { viewModel.deleteTag(tag) },
                                onNavigate = { 
                                    // 导航到该标签的第一个位置
                                    uiState.locations.firstOrNull { it.tagId == tag.id }?.let { location ->
                                        viewModel.startNavigationToLocation(location)
                                    }
                                },
                                locationCount = uiState.locations.count { it.tagId == tag.id }
                            )
                        }
                    }
                }
            }
        }
        
        // 消息提示
        uiState.errorMessage?.let { message ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(message)
                Button(onClick = { viewModel.clearMessages() }) {
                    Text("确定")
                }
            }
        }
        
        uiState.successMessage?.let { message ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = Color.Green
            ) {
                Text(message, color = Color.White)
                Button(
                    onClick = { viewModel.clearMessages() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("确定", color = Color.Green)
                }
            }
        }
        
        // 加载指示器 - 只在初始化时显示，避免遮挡地图
        if (uiState.isLoading && !uiState.isMapLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "正在加载地图...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        // 定位指示器 - 不遮挡地图
        if (uiState.isLocating) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "定位中...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
    
    // 标签创建对话框
    if (uiState.showTagDialog) {
        TagCreateDialog(
            onDismiss = { viewModel.hideTagDialog() },
            onConfirm = { name, color, description ->
                viewModel.createTag(name, color, description)
            }
        )
    }
    
    // 位置标记对话框
    if (uiState.showLocationDialog && uiState.selectedLocation != null) {
        LocationCreateDialog(
            location = uiState.selectedLocation!!,
            tags = uiState.tags,
            selectedTag = selectedTag,
            onDismiss = { viewModel.hideLocationDialog() },
            onConfirm = { name, tagId, description, address ->
                viewModel.createLocation(
                    name = name,
                    latitude = uiState.selectedLocation!!.latitude,
                    longitude = uiState.selectedLocation!!.longitude,
                    tagId = tagId,
                    description = description,
                    address = address
                )
            }
        )
    }
}

@Composable
private fun TagItem(
    tag: MapTag,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onNavigate: () -> Unit = {},
    locationCount: Int = 0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) try {
                    Color(android.graphics.Color.parseColor(tag.color)).copy(alpha = 0.2f)
                } catch (e: Exception) {
                    Color.Gray.copy(alpha = 0.1f)
                }
                else Color.Gray.copy(alpha = 0.1f)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(
                    try {
                        Color(android.graphics.Color.parseColor(tag.color))
                    } catch (e: Exception) {
                        Color.Gray
                    },
                    RoundedCornerShape(4.dp)
                )
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = tag.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        
        // 位置数量显示
        if (locationCount > 0) {
            Text(
                text = "$locationCount",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        
        IconButton(onClick = onNavigate) {
            Icon(
                Icons.Default.Navigation,
                "导航到此标签",
                tint = Color.Blue,
                modifier = Modifier.size(20.dp)
            )
        }
        
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                "删除标签",
                tint = Color.Red,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TagCreateDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: String, description: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("#FF5722") }
    var description by remember { mutableStateOf("") }
    
    val predefinedColors = listOf(
        "#F57C00", "#4CAF50", "#E91E63", "#2196F3",
        "#9C27B0", "#795548", "#FF5722", "#607D8B"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建标签") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("标签名称") },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("选择颜色:")
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    predefinedColors.forEach { colorValue ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(android.graphics.Color.parseColor(colorValue)), RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .let { modifier ->
                                    if (color == colorValue) {
                                        modifier.border(2.dp, Color.Black, RoundedCornerShape(16.dp))
                                    } else modifier
                                }
                                .clickable { color = colorValue }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, color, description.ifBlank { null })
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun LocationCreateDialog(
    location: LatLng,
    tags: List<MapTag>,
    selectedTag: MapTag?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, tagId: Long, description: String?, address: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedTagId by remember { mutableStateOf(selectedTag?.id ?: tags.firstOrNull()?.id ?: 0L) }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加位置标记") },
        text = {
            Column {
                Text(
                    "坐标: ${location.latitude.toFixed(6)}, ${location.longitude.toFixed(6)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("位置名称") },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("选择标签:")
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.height(120.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(tags) { tag ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (selectedTagId == tag.id)
                                        Color(android.graphics.Color.parseColor(tag.color)).copy(alpha = 0.2f)
                                    else Color.Transparent
                                )
                                .clickable { selectedTagId = tag.id }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color(android.graphics.Color.parseColor(tag.color)), RoundedCornerShape(2.dp))
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = tag.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("地址（可选）") },
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && selectedTagId != 0L) {
                        onConfirm(
                            name,
                            selectedTagId,
                            description.ifBlank { null },
                            address.ifBlank { null }
                        )
                    }
                },
                enabled = name.isNotBlank() && selectedTagId != 0L
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun Double.toFixed(digits: Int): String {
    return "%.${digits}f".format(this)
}