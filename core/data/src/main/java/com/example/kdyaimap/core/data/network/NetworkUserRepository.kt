package com.example.kdyaimap.core.data.network

import com.example.kdyaimap.core.model.User
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * 用户网络仓库
 * 处理用户相关的网络请求
 */
@Singleton
class NetworkUserRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    /**
     * 用户登录
     */
    suspend fun login(username: String, password: String): Result<User> {
        return try {
            android.util.Log.d("NetworkUserRepository", "开始登录请求: username=$username")
            val response = apiService.login(
                ApiService.LoginRequest(username, password)
            )
            
            android.util.Log.d("NetworkUserRepository", "登录响应: code=${response.code()}, successful=${response.isSuccessful}")
            
            response.handleResponse().onSuccess { loginResponse ->
                // 保存Token到RetrofitClient
                RetrofitClient.setAuthToken(loginResponse.token)
                android.util.Log.d("NetworkUserRepository", "登录成功，token已保存")
                // 这里还可以保存Token到本地存储
            }.map { it.user }
        } catch (e: Exception) {
            android.util.Log.e("NetworkUserRepository", "登录失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 用户注册
     */
    suspend fun register(
        username: String,
        password: String,
        email: String,
        role: String = "STUDENT"
    ): Result<User> {
        return try {
            android.util.Log.d("NetworkUserRepository", "开始注册请求: username=$username, email=$email")
            val response = apiService.register(
                ApiService.RegisterRequest(username, password, email, role)
            )
            android.util.Log.d("NetworkUserRepository", "注册响应: code=${response.code()}, successful=${response.isSuccessful}")
            response.handleResponse()
        } catch (e: Exception) {
            android.util.Log.e("NetworkUserRepository", "注册失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 根据ID获取用户信息
     */
    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val response = apiService.getUserById(userId)
            response.handleResponse()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新用户信息
     */
    suspend fun updateUser(
        userId: String,
        username: String? = null,
        email: String? = null,
        avatar: String? = null,
        bio: String? = null,
        phone: String? = null
    ): Result<User> {
        return try {
            val response = apiService.updateUser(
                userId,
                ApiService.UpdateUserRequest(username, email, avatar, bio, phone)
            )
            response.handleResponse()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有用户
     */
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val response = apiService.getAllUsers()
            response.handleResponse()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 关注用户
     */
    suspend fun followUser(userId: String, targetUserId: String): Result<Unit> {
        return try {
            val response = apiService.followUser(userId, targetUserId)
            response.handleResponse().map { }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 取消关注
     */
    suspend fun unfollowUser(userId: String, targetUserId: String): Result<Unit> {
        return try {
            val response = apiService.unfollowUser(userId, targetUserId)
            response.handleResponse().map { }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取用户关注列表
     */
    suspend fun getUserFollowing(userId: String): Result<List<User>> {
        return try {
            val response = apiService.getUserFollowing(userId)
            response.handleResponse()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取用户粉丝列表
     */
    suspend fun getUserFollowers(userId: String): Result<List<User>> {
        return try {
            val response = apiService.getUserFollowers(userId)
            response.handleResponse()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 检查关注状态
     */
    suspend fun checkFollowStatus(userId: String, targetUserId: String): Result<Boolean> {
        return try {
            val response = apiService.checkFollowStatus(userId, targetUserId)
            response.handleResponse()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 上传头像
     */
    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray, fileName: String): Result<String> {
        return try {
            val requestBody = okhttp3.MultipartBody.Builder()
                .setType(okhttp3.MultipartBody.FORM)
                .addFormDataPart("avatar", fileName,
                    imageBytes.toRequestBody("image/*".toMediaType())
                )
                .build()
            
            val imagePart = requestBody.parts.first()
            val response = apiService.uploadAvatar(userId, imagePart)
            response.handleResponse()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 用户登出
     */
    fun logout() {
        RetrofitClient.clearAuthToken()
        // 这里还可以清除本地存储的Token
    }
    
    /**
     * 检查登录状态
     */
    fun isLoggedIn(): Boolean {
        return RetrofitClient.isLoggedIn()
    }
}