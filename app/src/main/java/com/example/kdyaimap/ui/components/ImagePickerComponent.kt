package com.example.kdyaimap.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kdyaimap.util.ImageUtils
import kotlinx.coroutines.launch

/**
 * 图片选择器组件
 */
@Composable
fun ImagePicker(
    images: List<String>,
    maxImages: Int = 3,
    onImagesChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // 单张图片选择器
    val singleImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            if (ImageUtils.isValidImageUri(context, it)) {
                if (ImageUtils.isImageSizeValid(context, it)) {
                    // 压缩图片
                    coroutineScope.launch {
                        val compressedBytes = ImageUtils.compressImage(context, it)
                        compressedBytes?.let { bytes ->
                            // 这里应该上传到服务器，现在先保存本地URI
                            onImagesChanged(images + it.toString())
                        }
                    }
                } else {
                    // 图片过大，显示错误提示
                }
            } else {
                // 无效的图片格式
            }
        }
    }
    
    // 多张图片选择器
    val multipleImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val validUris = uris.filter { uri ->
            ImageUtils.isValidImageUri(context, uri) && ImageUtils.isImageSizeValid(context, uri)
        }
        
        if (validUris.isNotEmpty()) {
            coroutineScope.launch {
                val compressedImages = ImageUtils.compressImages(context, validUris)
                val imageUris = compressedImages.mapNotNull { bytes ->
                    if (bytes != null) {
                        // 这里应该上传到服务器，现在先保存临时文件
                        val tempFile = ImageUtils.saveImageToTempFile(context, bytes)
                        tempFile?.toString()
                    } else null
                }
                onImagesChanged(images + imageUris.take(maxImages - images.size))
            }
        }
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "活动图片 (最多${maxImages}张)",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        // 图片网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(120.dp * ((images.size + 2) / 3))
        ) {
            items(images.size) { index ->
                val imageUrl = images[index]
                ImageItem(
                    imageUrl = imageUrl,
                    onDelete = {
                        onImagesChanged(images - imageUrl)
                    },
                    enabled = enabled
                )
            }
            
            // 添加图片按钮
            if (images.size < maxImages && enabled) {
                item {
                    AddImageButton(
                        onClick = {
                            if (maxImages > 1) {
                                multipleImagePicker.launch("image/*")
                            } else {
                                singleImagePicker.launch("image/*")
                            }
                        }
                    )
                }
            }
        }
        
        // 图片选择提示
        if (images.size < maxImages && enabled) {
            Text(
                text = "点击添加图片，支持JPG、PNG格式，单张不超过5MB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 单个图片项
 */
@Composable
private fun ImageItem(
    imageUrl: String,
    onDelete: () -> Unit,
    enabled: Boolean
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
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
        
        // 删除按钮
        if (enabled) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "删除图片",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * 添加图片按钮
 */
@Composable
private fun AddImageButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "添加图片",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "添加图片",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 头像选择器
 */
@Composable
fun AvatarPicker(
    avatarUrl: String?,
    onAvatarChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 80.dp,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            if (ImageUtils.isValidImageUri(context, it)) {
                if (ImageUtils.isImageSizeValid(context, it)) {
                    isUploading = true
                    coroutineScope.launch {
                        val compressedBytes = ImageUtils.compressImage(context, it, 400, 400, 90)
                        compressedBytes?.let { bytes ->
                            // 这里应该上传到服务器
                            val tempFile = ImageUtils.saveImageToTempFile(context, bytes)
                            tempFile?.let { file ->
                                onAvatarChanged(file.toString())
                            }
                        }
                        isUploading = false
                    }
                } else {
                    // 图片过大
                }
            } else {
                // 无效格式
            }
        }
    }
    
    Box(
        modifier = modifier
            .size(size)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .then(if (enabled) Modifier.clickable { imagePicker.launch("image/*") } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl?.isNotEmpty() == true) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "头像",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                Icons.Default.Person,
                contentDescription = "默认头像",
                modifier = Modifier.size(size * 0.5f),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        // 上传指示器
        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(size * 0.3f),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            }
        }
        
        // 编辑提示
        if (enabled && !isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "点击更换",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

/**
 * 图片预览对话框
 */
@Composable
fun ImagePreviewDialog(
    imageUrl: String,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "图片预览",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}