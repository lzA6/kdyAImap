package com.example.kdyaimap.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.kdyaimap.core.model.TradingCategory
import com.example.kdyaimap.core.model.ItemCondition
import com.example.kdyaimap.core.model.ContactType
import com.example.kdyaimap.core.model.getDisplayName
import com.example.kdyaimap.ui.viewmodel.TradingViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTradingPostScreen(
    onBack: () -> Unit,
    onPostCreated: () -> Unit,
    tradingViewModel: TradingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val tradingState by tradingViewModel.tradingState.collectAsState()
    
    // 表单状态
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TradingCategory.ELECTRONICS) }
    var price by remember { mutableStateOf("") }
    var isNegotiable by remember { mutableStateOf(false) }
    var selectedCondition by remember { mutableStateOf(ItemCondition.GOOD) }
    var selectedImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var location by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(39.9042) } // 默认北京坐标
    var longitude by remember { mutableStateOf(116.4074) }
    var contactInfo by remember { mutableStateOf("") }
    var selectedContactType by remember { mutableStateOf(ContactType.PHONE) }
    var isAnonymous by remember { mutableStateOf(false) }
    var tags by remember { mutableStateOf("") }
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImages = selectedImages + it.toString()
        }
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
                text = "发布二手商品",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 商品标题
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("商品标题 *") },
            modifier = Modifier.fillMaxWidth(),
            isError = title.isEmpty() && tradingState is com.example.kdyaimap.ui.viewmodel.TradingState.Error,
            supportingText = if (title.isEmpty() && tradingState is com.example.kdyaimap.ui.viewmodel.TradingState.Error) {
                { Text("请输入商品标题", color = MaterialTheme.colorScheme.error) }
            } else null
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 商品分类
        Text("商品分类 *", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        
        // 分类选择网格
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TradingCategory.values()) { category ->
                FilterChip(
                    onClick = { selectedCategory = category },
                    label = { Text(category.getDisplayName()) },
                    selected = selectedCategory == category
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 价格和议价
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = price,
                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) price = it },
                label = { Text("价格 (元) *") },
                modifier = Modifier.weight(1f),
                isError = price.isEmpty() && tradingState is com.example.kdyaimap.ui.viewmodel.TradingState.Error
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Checkbox(
                    checked = isNegotiable,
                    onCheckedChange = { isNegotiable = it }
                )
                Text("可议价")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 商品状况
        Text("商品状况 *", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ItemCondition.values()) { condition ->
                FilterChip(
                    onClick = { selectedCondition = condition },
                    label = { Text(condition.getDisplayName()) },
                    selected = selectedCondition == condition
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 商品描述
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("商品描述 *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 6,
            isError = description.isEmpty() && tradingState is com.example.kdyaimap.ui.viewmodel.TradingState.Error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 商品图片
        Text("商品图片", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
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
                        contentDescription = "商品图片",
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 交易地点
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("交易地点 *") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { /* TODO: 打开地图选择位置 */ }) {
                    Icon(Icons.Default.LocationOn, contentDescription = "选择位置")
                }
            },
            isError = location.isEmpty() && tradingState is com.example.kdyaimap.ui.viewmodel.TradingState.Error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 联系方式
        Text("联系方式 *", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        
        // 联系类型选择
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ContactType.values()) { contactType ->
                FilterChip(
                    onClick = { selectedContactType = contactType },
                    label = { Text(contactType.getDisplayName()) },
                    selected = selectedContactType == contactType
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = contactInfo,
            onValueChange = { contactInfo = it },
            label = { Text("联系信息 *") },
            modifier = Modifier.fillMaxWidth(),
            isError = contactInfo.isEmpty() && tradingState is com.example.kdyaimap.ui.viewmodel.TradingState.Error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 标签
        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            label = { Text("标签 (用空格分隔)") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例如: 全新 苹果 手机") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 匿名发布
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isAnonymous,
                onCheckedChange = { isAnonymous = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("匿名发布")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 发布按钮
        Button(
            onClick = {
                if (validateForm(title, description, price, location, contactInfo)) {
                    val tagsList = if (tags.isBlank()) emptyList() else tags.split(" ").filter { it.isNotBlank() }
                    
                    tradingViewModel.createPost(
                        title = title,
                        description = description,
                        category = selectedCategory,
                        price = price.toDoubleOrNull() ?: 0.0,
                        isNegotiable = isNegotiable,
                        condition = selectedCondition,
                        images = selectedImages,
                        location = location,
                        latitude = latitude,
                        longitude = longitude,
                        contactInfo = contactInfo,
                        contactType = selectedContactType,
                        authorId = 1L, // TODO: 从用户状态获取
                        authorName = if (isAnonymous) "匿名用户" else "当前用户", // TODO: 从用户状态获取
                        isAnonymous = isAnonymous,
                        tags = tagsList
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = tradingState !is com.example.kdyaimap.ui.viewmodel.TradingState.Loading
        ) {
            if (tradingState is com.example.kdyaimap.ui.viewmodel.TradingState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("发布商品")
            }
        }
        
        // 状态显示
        when (val state = tradingState) {
            is com.example.kdyaimap.ui.viewmodel.TradingState.Loading -> {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            is com.example.kdyaimap.ui.viewmodel.TradingState.PostCreated -> {
                LaunchedEffect(Unit) {
                    onPostCreated()
                }
            }
            is com.example.kdyaimap.ui.viewmodel.TradingState.Error -> {
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
            else -> {}
        }
    }
}

private fun validateForm(
    title: String,
    description: String,
    price: String,
    location: String,
    contactInfo: String
): Boolean {
    return title.isNotBlank() && 
           description.isNotBlank() && 
           price.isNotBlank() && 
           location.isNotBlank() && 
           contactInfo.isNotBlank()
}