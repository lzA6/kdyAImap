package com.example.kdyaimap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kdyaimap.core.model.User
import com.example.kdyaimap.core.data.repository.UserPreferencesRepository
import com.example.kdyaimap.domain.usecase.GetUserByIdUseCase
import com.example.kdyaimap.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val userRepository: UserRepository,
    private val userManager: UserManager
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _socialState = MutableStateFlow<SocialState>(SocialState.Idle)
    val socialState: StateFlow<SocialState> = _socialState.asStateFlow()

    init {
        loadCurrentUser()
    }

    // ==================== 用户信息相关 ====================

    private fun loadCurrentUser() {
        viewModelScope.launch {
            userManager.currentUser.collect { user ->
                if (user != null) {
                    _profileState.value = ProfileState.Success(user)
                } else {
                    _profileState.value = ProfileState.Error("用户信息加载失败")
                }
            }
        }
    }
    
    fun refresh() {
        loadCurrentUser()
    }

    /**
     * 更新用户信息
     */
    fun updateUserProfile(
        username: String? = null,
        email: String? = null,
        avatar: String? = null,
        bio: String? = null,
        phone: String? = null
    ) {
        viewModelScope.launch {
            val currentState = _profileState.value
            if (currentState is ProfileState.Success) {
                _profileState.value = ProfileState.Loading
                
                try {
                    val result = userRepository.updateUserNetwork(
                        userId = currentState.user.id.toString(),
                        username = username,
                        email = email,
                        avatar = avatar,
                        bio = bio,
                        phone = phone
                    )
                    
                    result.onSuccess { updatedUser ->
                        _profileState.value = ProfileState.Success(updatedUser)
                    }.onFailure { exception ->
                        _profileState.value = ProfileState.Error(
                            exception.message ?: "更新用户信息失败"
                        )
                    }
                } catch (e: Exception) {
                    _profileState.value = ProfileState.Error("更新用户信息失败：${e.message}")
                }
            }
        }
    }

    /**
     * 上传头像
     */
    fun uploadAvatar(imageBytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            val currentState = _profileState.value
            if (currentState is ProfileState.Success) {
                _socialState.value = SocialState.Loading
                
                try {
                    val result = userRepository.uploadAvatar(
                        userId = currentState.user.id.toString(),
                        imageBytes = imageBytes,
                        fileName = fileName
                    )
                    
                    result.onSuccess { avatarUrl ->
                        // 头像上传成功后，更新用户信息
                        updateUserProfile(avatar = avatarUrl)
                        _socialState.value = SocialState.AvatarUploadSuccess(avatarUrl)
                    }.onFailure { exception ->
                        _socialState.value = SocialState.Error(
                            exception.message ?: "上传头像失败"
                        )
                    }
                } catch (e: Exception) {
                    _socialState.value = SocialState.Error("上传头像失败：${e.message}")
                }
            }
        }
    }

    // ==================== 社交功能相关 ====================

    /**
     * 关注用户
     */
    fun followUser(targetUserId: String) {
        viewModelScope.launch {
            val currentState = _profileState.value
            if (currentState is ProfileState.Success) {
                _socialState.value = SocialState.Loading
                
                try {
                    val result = userRepository.followUser(
                        userId = currentState.user.id.toString(),
                        targetUserId = targetUserId
                    )
                    
                    result.onSuccess {
                        _socialState.value = SocialState.FollowSuccess
                        // 重新加载用户信息以更新关注数
                        refresh()
                    }.onFailure { exception ->
                        _socialState.value = SocialState.Error(
                            exception.message ?: "关注失败"
                        )
                    }
                } catch (e: Exception) {
                    _socialState.value = SocialState.Error("关注失败：${e.message}")
                }
            }
        }
    }

    /**
     * 取消关注
     */
    fun unfollowUser(targetUserId: String) {
        viewModelScope.launch {
            val currentState = _profileState.value
            if (currentState is ProfileState.Success) {
                _socialState.value = SocialState.Loading
                
                try {
                    val result = userRepository.unfollowUser(
                        userId = currentState.user.id.toString(),
                        targetUserId = targetUserId
                    )
                    
                    result.onSuccess {
                        _socialState.value = SocialState.UnfollowSuccess
                        // 重新加载用户信息以更新关注数
                        refresh()
                    }.onFailure { exception ->
                        _socialState.value = SocialState.Error(
                            exception.message ?: "取消关注失败"
                        )
                    }
                } catch (e: Exception) {
                    _socialState.value = SocialState.Error("取消关注失败：${e.message}")
                }
            }
        }
    }

    /**
     * 获取用户关注列表
     */
    fun getUserFollowing(userId: String) {
        viewModelScope.launch {
            _socialState.value = SocialState.Loading
            
            try {
                val result = userRepository.getUserFollowing(userId)
                result.onSuccess { users ->
                    _socialState.value = SocialState.FollowingList(users)
                }.onFailure { exception ->
                    _socialState.value = SocialState.Error(
                        exception.message ?: "获取关注列表失败"
                    )
                }
            } catch (e: Exception) {
                _socialState.value = SocialState.Error("获取关注列表失败：${e.message}")
            }
        }
    }

    /**
     * 获取用户粉丝列表
     */
    fun getUserFollowers(userId: String) {
        viewModelScope.launch {
            _socialState.value = SocialState.Loading
            
            try {
                val result = userRepository.getUserFollowers(userId)
                result.onSuccess { users ->
                    _socialState.value = SocialState.FollowersList(users)
                }.onFailure { exception ->
                    _socialState.value = SocialState.Error(
                        exception.message ?: "获取粉丝列表失败"
                    )
                }
            } catch (e: Exception) {
                _socialState.value = SocialState.Error("获取粉丝列表失败：${e.message}")
            }
        }
    }

    /**
     * 检查关注状态
     */
    fun checkFollowStatus(targetUserId: String) {
        viewModelScope.launch {
            val currentState = _profileState.value
            if (currentState is ProfileState.Success) {
                try {
                    val result = userRepository.checkFollowStatus(
                        userId = currentState.user.id.toString(),
                        targetUserId = targetUserId
                    )
                    result.onSuccess { isFollowing ->
                        _socialState.value = SocialState.FollowStatus(isFollowing)
                    }.onFailure { exception ->
                        _socialState.value = SocialState.Error(
                            exception.message ?: "检查关注状态失败"
                        )
                    }
                } catch (e: Exception) {
                    _socialState.value = SocialState.Error("检查关注状态失败：${e.message}")
                }
            }
        }
    }

    /**
     * 重置社交状态
     */
    fun resetSocialState() {
        _socialState.value = SocialState.Idle
    }
}

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val user: User) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class SocialState {
    object Idle : SocialState()
    object Loading : SocialState()
    object FollowSuccess : SocialState()
    object UnfollowSuccess : SocialState()
    data class FollowStatus(val isFollowing: Boolean) : SocialState()
    data class FollowingList(val users: List<User>) : SocialState()
    data class FollowersList(val users: List<User>) : SocialState()
    data class AvatarUploadSuccess(val avatarUrl: String) : SocialState()
    data class Error(val message: String) : SocialState()
}