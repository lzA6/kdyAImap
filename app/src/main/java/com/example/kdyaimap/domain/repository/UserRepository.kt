package com.example.kdyaimap.domain.repository

import com.example.kdyaimap.core.model.User
import com.example.kdyaimap.core.model.UserRole
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    // ==================== 本地数据库方法 ====================
    suspend fun registerUser(user: User)
    suspend fun getUserByUsername(username: String): User?
    fun getAllUsers(): Flow<List<User>>
    suspend fun updateUserRole(userId: Long, role: UserRole)
    suspend fun getUserById(userId: Long): User?
    
    // ==================== 网络请求方法 ====================
    
    /**
     * 用户登录
     */
    suspend fun login(username: String, password: String): Result<User>
    
    /**
     * 用户注册（网络版）
     */
    suspend fun registerNetwork(
        username: String,
        password: String,
        email: String,
        role: String = "STUDENT"
    ): Result<User>
    
    /**
     * 根据ID获取用户信息（网络版）
     */
    suspend fun getUserByIdNetwork(userId: String): Result<User>
    
    /**
     * 更新用户信息（网络版）
     */
    suspend fun updateUserNetwork(
        userId: String,
        username: String? = null,
        email: String? = null,
        avatar: String? = null,
        bio: String? = null,
        phone: String? = null
    ): Result<User>
    
    /**
     * 获取所有用户（网络版）
     */
    suspend fun getAllUsersNetwork(): Result<List<User>>
    
    /**
     * 关注用户
     */
    suspend fun followUser(userId: String, targetUserId: String): Result<Unit>
    
    /**
     * 取消关注
     */
    suspend fun unfollowUser(userId: String, targetUserId: String): Result<Unit>
    
    /**
     * 获取用户关注列表
     */
    suspend fun getUserFollowing(userId: String): Result<List<User>>
    
    /**
     * 获取用户粉丝列表
     */
    suspend fun getUserFollowers(userId: String): Result<List<User>>
    
    /**
     * 检查关注状态
     */
    suspend fun checkFollowStatus(userId: String, targetUserId: String): Result<Boolean>
    
    /**
     * 上传头像
     */
    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray, fileName: String): Result<String>
    
    /**
     * 用户登出
     */
    fun logout()
    
    /**
     * 检查登录状态
     */
    fun isLoggedIn(): Boolean
}