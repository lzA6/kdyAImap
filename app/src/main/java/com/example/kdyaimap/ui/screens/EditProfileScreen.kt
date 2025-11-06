package com.example.kdyaimap.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.kdyaimap.ui.viewmodel.ProfileViewModel
import com.example.kdyaimap.ui.viewmodel.ProfileState
import com.example.kdyaimap.ui.viewmodel.SocialState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val profileState by profileViewModel.profileState.collectAsState()
    val socialState by profileViewModel.socialState.collectAsState()
    val context = LocalContext.current
    
    // 表单状态
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
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

    // 监听用户信息变化，更新表单
    LaunchedEffect(profileState) {
        when (val state = profileState) {
            is ProfileState.Success -> {
                val user = state.user
                username = user.username
                email = user.email
                bio = user.bio
                phone = user.phone
                avatarUrl = user.avatar
            }
            else -> {}
        }
    }

    // 监听社交状态变化
    LaunchedEffect(socialState) {
        when (val state = socialState) {
            is SocialState.AvatarUploadSuccess -> {
                avatarUrl = state.avatarUrl
                profileViewModel.resetSocialState()
            }
            is SocialState.Error -> {
                profileViewModel.resetSocialState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑资料") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            profileViewModel.updateUserProfile(
                                username = username.ifBlank { null },
                                email = email.ifBlank { null },
                                bio = bio.ifBlank { null },
                                phone = phone.ifBlank { null }
                            )
                            onSaveSuccess()
                        }
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = profileState) {
            is ProfileState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is ProfileState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 头像编辑
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUrl.isNotEmpty()) {
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
                                modifier = Modifier.size(60.dp),
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
                                    modifier = Modifier.size(32.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp
                                )
                            }
                        }
                        
                        // 相机图标
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .align(Alignment.BottomEnd)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "更换头像",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 用户名
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("用户名") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 邮箱
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("邮箱") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 电话
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("电话") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null)
                        },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 个人简介
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("个人简介") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Description, contentDescription = null)
                        },
                        maxLines = 4
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // 保存按钮
                    Button(
                        onClick = {
                            profileViewModel.updateUserProfile(
                                username = username.ifBlank { null },
                                email = email.ifBlank { null },
                                bio = bio.ifBlank { null },
                                phone = phone.ifBlank { null }
                            )
                            onSaveSuccess()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = socialState !is SocialState.Loading
                    ) {
                        if (socialState is SocialState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("保存修改")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 取消按钮
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("取消")
                    }
                }
            }
            
            is ProfileState.Error -> {
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
                        Button(onClick = { profileViewModel.refresh() }) {
                            Text("重试")
                        }
                    }
                }
            }
        }
    }
}