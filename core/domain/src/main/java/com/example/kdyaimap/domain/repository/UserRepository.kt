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
    suspend fun login(username: String, password: String): Result<User>
    suspend fun registerNetwork(
        username: String,
        password: String,
        email: String,
        role: String
    ): Result<User>
    suspend fun getUserByIdNetwork(userId: String): Result<User>
    suspend fun updateUserNetwork(
        userId: String,
        username: String?,
        email: String?,
        avatar: String?,
        bio: String?,
        phone: String?
    ): Result<User>
    suspend fun getAllUsersNetwork(): Result<List<User>>
    suspend fun followUser(userId: String, targetUserId: String): Result<Unit>
    suspend fun unfollowUser(userId: String, targetUserId: String): Result<Unit>
    suspend fun getUserFollowing(userId: String): Result<List<User>>
    suspend fun getUserFollowers(userId: String): Result<List<User>>
    suspend fun checkFollowStatus(userId: String, targetUserId: String): Result<Boolean>
    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray, fileName: String): Result<String>
    fun logout()
    fun isLoggedIn(): Boolean
}