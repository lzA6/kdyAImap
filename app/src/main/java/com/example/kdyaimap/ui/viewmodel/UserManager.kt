package com.example.kdyaimap.ui.viewmodel

import com.example.kdyaimap.core.model.User
import com.example.kdyaimap.core.model.UserRole
import com.example.kdyaimap.core.data.repository.UserPreferencesRepository
import com.example.kdyaimap.domain.usecase.GetUserByIdUseCase
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // 创建一个专用的协程作用域
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        initializeAnonymousUser()
    }

    private fun initializeAnonymousUser() {
        // 使用专用的协程作用域
        scope.launch {
            try {
                // 检查是否已有缓存的用户信息
                val userId = userPreferencesRepository.userId.first()
                if (userId != null) {
                    val existingUser = getUserByIdUseCase(userId.toLong())
                    if (existingUser != null) {
                        _currentUser.value = existingUser
                        return@launch
                    }
                }
                
                // 创建默认匿名用户
                val anonymousUser = User(
                    username = "游客用户",
                    passwordHash = "",
                    role = UserRole.STUDENT,
                    contactInfo = ByteArray(0),
                    iv = ByteArray(0),
                    isAnonymous = true,
                    anonymousId = "guest_${UUID.randomUUID().toString().substring(0, 8)}",
                    bio = "这是一个游客用户，无需登录即可使用应用"
                )
                
                _currentUser.value = anonymousUser
                
            } catch (e: Exception) {
                // 如果出错，创建一个最基本的用户对象
                val fallbackUser = User(
                    username = "游客",
                    passwordHash = "",
                    role = UserRole.STUDENT,
                    contactInfo = ByteArray(0),
                    iv = ByteArray(0),
                    isAnonymous = true,
                    anonymousId = "fallback_guest"
                )
                _currentUser.value = fallbackUser
            }
        }
    }

    fun getCurrentUser(): User? {
        return _currentUser.value
    }

    fun isCurrentUserAnonymous(): Boolean {
        return _currentUser.value?.isAnonymous ?: true
    }
}