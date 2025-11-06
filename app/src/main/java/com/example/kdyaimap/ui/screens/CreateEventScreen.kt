package com.example.kdyaimap.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kdyaimap.core.model.EventType
import com.example.kdyaimap.core.model.EventStatus
import com.example.kdyaimap.ui.viewmodel.EventViewModel
import com.example.kdyaimap.ui.viewmodel.EventState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    eventViewModel: EventViewModel = hiltViewModel(),
    onEventCreated: () -> Unit,
    onBack: () -> Unit
) {
    val eventState by eventViewModel.eventState.collectAsState()
    
    // 表单状态
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedEventType by remember { mutableStateOf(EventType.TRADE) }
    var locationName by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(39.9042) } // 默认北京坐标
    var longitude by remember { mutableStateOf(116.4074) }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // 这里应该上传图片到服务器，现在先保存本地路径
            selectedImages = selectedImages + it.toString()
        }
    }
    
    // 日期时间选择器
    val context = LocalContext.current
    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val timePickerDialog = android.app.TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val calendar = Calendar.getInstance()
                        calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                        val formattedTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            .format(calendar.time)
                        if (startTime.isEmpty()) {
                            startTime = formattedTime
                        } else {
                            endTime = formattedTime
                        }
                    },
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE),
                    true
                )
                timePickerDialog.show()
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 顶部栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "创建新活动",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 活动标题
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("活动标题 *") },
            modifier = Modifier.fillMaxWidth(),
            isError = title.isEmpty() && eventState is EventState.Error,
            supportingText = if (title.isEmpty() && eventState is EventState.Error) {
                { Text("请输入活动标题", color = MaterialTheme.colorScheme.error) }
            } else null
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 活动类型选择
        Text("活动类型 *", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EventType.values().forEach { type ->
                FilterChip(
                    onClick = { selectedEventType = type },
                    label = { Text(getEventTypeName(type)) },
                    selected = selectedEventType == type,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 活动描述
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("活动描述 *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 6,
            isError = description.isEmpty() && eventState is EventState.Error,
            supportingText = if (description.isEmpty() && eventState is EventState.Error) {
                { Text("请输入活动描述", color = MaterialTheme.colorScheme.error) }
            } else null
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 活动地点
        OutlinedTextField(
            value = locationName,
            onValueChange = { locationName = it },
            label = { Text("活动地点 *") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { /* TODO: 打开地图选择位置 */ }) {
                    Icon(Icons.Default.LocationOn, contentDescription = "选择位置")
                }
            },
            isError = locationName.isEmpty() && eventState is EventState.Error,
            supportingText = if (locationName.isEmpty() && eventState is EventState.Error) {
                { Text("请输入活动地点", color = MaterialTheme.colorScheme.error) }
            } else null
        )
        
        // 显示坐标信息
        if (latitude != 39.9042 || longitude != 116.4074) {
            Text(
                text = "坐标: $latitude, $longitude",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 时间设置
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = startTime,
                onValueChange = { },
                label = { Text("开始时间 *") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.Schedule, contentDescription = "选择时间")
                    }
                },
                isError = startTime.isEmpty() && eventState is EventState.Error
            )
            
            OutlinedTextField(
                value = endTime,
                onValueChange = { },
                label = { Text("结束时间") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.Schedule, contentDescription = "选择时间")
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 最大参与人数（仅对某些活动类型显示）
        if (selectedEventType == EventType.SOCIAL || selectedEventType == EventType.STUDY) {
            OutlinedTextField(
                value = maxParticipants,
                onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) maxParticipants = it },
                label = { Text("最大参与人数") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Text("人") }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 图片上传
        Text("活动图片", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 已选择的图片
            selectedImages.take(3).forEach { imageUrl ->
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "活动图片",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = {
                            selectedImages = selectedImages - imageUrl
                        },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "删除图片",
                            tint = Color.White,
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f))
                        )
                    }
                }
            }
            
            // 添加图片按钮
            if (selectedImages.size < 3) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "添加图片",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "添加图片",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        
        if (selectedImages.size >= 3) {
            Text(
                text = "最多上传3张图片",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 创建按钮
        Button(
            onClick = {
                if (validateForm(title, description, locationName, startTime)) {
                    val startTimeMillis = parseTimeToMillis(startTime)
                    val endTimeMillis = if (endTime.isNotEmpty()) parseTimeToMillis(endTime) else null
                    val maxParticipantsValue = if (maxParticipants.isNotEmpty()) maxParticipants.toInt() else null
                    
                    eventViewModel.createEventNetwork(
                        title = title,
                        description = description,
                        eventType = selectedEventType.name,
                        location = locationName,
                        latitude = latitude,
                        longitude = longitude,
                        startTime = startTimeMillis,
                        endTime = endTimeMillis,
                        maxParticipants = maxParticipantsValue,
                        images = selectedImages,
                        organizerId = "current_user_id" // TODO: 从用户状态获取
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = eventState !is EventState.Loading
        ) {
            if (eventState is EventState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("创建活动")
            }
        }
        
        // 状态显示
        when (val state = eventState) {
            is EventState.Loading -> {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            is EventState.Success -> {
                LaunchedEffect(Unit) {
                    onEventCreated()
                }
            }
            is EventState.Error -> {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            is EventState.Idle -> {}
            is EventState.ImageUploadSuccess -> {
                LaunchedEffect(Unit) {
                    onEventCreated()
                }
            }
        }
    }
}

private fun validateForm(
    title: String,
    description: String,
    location: String,
    startTime: String
): Boolean {
    return title.isNotBlank() && 
           description.isNotBlank() && 
           location.isNotBlank() && 
           startTime.isNotBlank()
}

private fun parseTimeToMillis(timeString: String): Long {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        format.parse(timeString)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

private fun getEventTypeName(eventType: EventType): String {
    return when (eventType) {
        EventType.TRADE -> "二手交易"
        EventType.SOCIAL -> "社交邀约"
        EventType.HELP -> "生活互助"
        EventType.STUDY -> "学习交流"
        EventType.CLUB_ACTIVITY -> "社团活动"
        EventType.ACADEMIC -> "学术交流"
        EventType.SPORTS -> "体育运动"
        EventType.ENTERTAINMENT -> "娱乐活动"
        EventType.VOLUNTEER -> "志愿服务"
        EventType.FOOD -> "餐饮美食"
        EventType.SERVICE -> "生活服务"
        EventType.DORMITORY -> "宿舍相关"
        EventType.OTHER -> "其他"
    }
}