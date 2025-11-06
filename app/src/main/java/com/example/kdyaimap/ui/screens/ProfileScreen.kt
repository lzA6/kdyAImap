package com.example.kdyaimap.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kdyaimap.ui.viewmodel.ProfileViewModel
import com.example.kdyaimap.ui.viewmodel.ProfileState
import com.example.kdyaimap.ui.viewmodel.SocialState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToAdmin: () -> Unit,
    onNavigateToFollowers: () -> Unit = {},
    onNavigateToFollowing: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToUserEvents: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val profileState by profileViewModel.profileState.collectAsState()
    val socialState by profileViewModel.socialState.collectAsState()
    val context = LocalContext.current
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // 读取图片字节数组并上传
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    val fileName = "avatar_${System.currentTimeMillis()}.jpg"
                    profileViewModel.uploadAvatar(bytes, fileName)
                }
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    // 监听社交状态变化
    LaunchedEffect(socialState) {
        when (socialState) {
            is SocialState.AvatarUploadSuccess -> {
                // 头像上传成功，可以显示成功消息
                profileViewModel.resetSocialState()
            }
            is SocialState.Error -> {
                // 显示错误消息
                profileViewModel.resetSocialState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (val state = profileState) {
            is ProfileState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ProfileState.Success -> {
                val user = state.user
                
                // 头像和基本信息区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 头像
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.avatar.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(user.avatar)
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
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // 上传指示器
                        if (socialState is SocialState.Loading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // 用户名和角色
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (user.role) {
                                    com.example.kdyaimap.core.model.UserRole.STUDENT -> "学生"
                                    com.example.kdyaimap.core.model.UserRole.CLUB_ADMIN -> "社团管理员"
                                    com.example.kdyaimap.core.model.UserRole.SUPER_ADMIN -> "超级管理员"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (user.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "已认证",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    
                    // 编辑资料按钮
                    IconButton(onClick = onNavigateToEditProfile) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑资料")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 个人简介
                if (user.bio.isNotEmpty()) {
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 统计信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 发布数
                    StatItem(
                        title = "发布",
                        count = user.postsCount,
                        onClick = { /* 导航到用户发布 */ }
                    )
                    
                    // 粉丝数
                    StatItem(
                        title = "粉丝",
                        count = user.followersCount,
                        onClick = onNavigateToFollowers
                    )
                    
                    // 关注数
                    StatItem(
                        title = "关注",
                        count = user.followingCount,
                        onClick = onNavigateToFollowing
                    )
                    
                    // 获赞数
                    StatItem(
                        title = "获赞",
                        count = user.likesCount,
                        onClick = { /* 导航到获赞内容 */ }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 联系信息
                if (user.email.isNotEmpty() || user.phone.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "联系方式",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (user.email.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = "邮箱",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            
                            if (user.phone.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = "电话",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = user.phone,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 功能按钮
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToUserEvents,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("我的活动")
                    }
                    
                    Button(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("设置")
                    }
                    
                    if (user.role == com.example.kdyaimap.core.model.UserRole.SUPER_ADMIN) {
                        Button(
                            onClick = onNavigateToAdmin,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("管理面板")
                        }
                    }
                }
            }
            is ProfileState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { profileViewModel.refresh() }) {
                            Text("重试")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    count: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}