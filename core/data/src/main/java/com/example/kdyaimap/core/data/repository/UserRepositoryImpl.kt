package com.example.kdyaimap.core.data.repository

import com.example.kdyaimap.core.data.db.UserDao
import com.example.kdyaimap.core.model.User
import com.example.kdyaimap.core.model.UserRole
import com.example.kdyaimap.core.data.di.IoDispatcher
import com.example.kdyaimap.domain.repository.UserRepository
import com.example.kdyaimap.core.data.network.NetworkUserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val networkUserRepository: NetworkUserRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : UserRepository {

    // ==================== 本地数据库方法 ====================

    override suspend fun registerUser(user: User) {
        userDao.insertUser(user)
    }

    override suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().flowOn(dispatcher)
    }

    override suspend fun updateUserRole(userId: Long, role: UserRole) {
        userDao.updateUserRole(userId, role.name)
    }

    override suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId)
    }

    // ==================== 网络请求方法 ====================

    override suspend fun login(username: String, password: String): Result<User> {
        return withContext(dispatcher) {
            try {
                android.util.Log.d("UserRepositoryImpl", "尝试网络登录: username=$username")
                val result = networkUserRepository.login(username, password)
                result.onSuccess { user ->
                    // 登录成功后，将用户信息保存到本地数据库
                    userDao.insertUser(user)
                    android.util.Log.d("UserRepositoryImpl", "网络登录成功，用户已保存到本地")
                }
                result
            } catch (e: Exception) {
                android.util.Log.w("UserRepositoryImpl", "网络登录失败，尝试本地登录: ${e.message}")
                
                // 网络登录失败，尝试本地登录
                try {
                    val localUser = userDao.getUserByUsername(username)
                    if (localUser != null) {
                        android.util.Log.d("UserRepositoryImpl", "本地登录成功")
                        Result.success(localUser)
                    } else {
                        android.util.Log.w("UserRepositoryImpl", "本地未找到用户")
                        Result.failure(Exception("用户不存在，请先注册"))
                    }
                } catch (localException: Exception) {
                    android.util.Log.e("UserRepositoryImpl", "本地登录也失败", localException)
                    Result.failure(Exception("网络连接失败且本地登录失败: ${e.message}"))
                }
            }
        }
    }

    override suspend fun registerNetwork(
        username: String,
        password: String,
        email: String,
        role: String
    ): Result<User> {
        return withContext(dispatcher) {
            try {
                val result = networkUserRepository.register(username, password, email, role)
                result.onSuccess { user ->
                    // 注册成功后，将用户信息保存到本地数据库
                    userDao.insertUser(user)
                }
                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getUserByIdNetwork(userId: String): Result<User> {
        return withContext(dispatcher) {
            try {
                val result = networkUserRepository.getUserById(userId)
                result.onSuccess { user ->
                    // 获取成功后，更新本地数据库
                    userDao.insertUser(user)
                }
                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateUserNetwork(
        userId: String,
        username: String?,
        email: String?,
        avatar: String?,
        bio: String?,
        phone: String?
    ): Result<User> {
        return withContext(dispatcher) {
            try {
                val result = networkUserRepository.updateUser(userId, username, email, avatar, bio, phone)
                result.onSuccess { user ->
                    // 更新成功后，同步到本地数据库
                    userDao.insertUser(user)
                }
                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getAllUsersNetwork(): Result<List<User>> {
        return withContext(dispatcher) {
            try {
                val result = networkUserRepository.getAllUsers()
                result.onSuccess { users ->
                    // 获取成功后，批量更新本地数据库
                    users.forEach { user ->
                        userDao.insertUser(user)
                    }
                }
                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun followUser(userId: String, targetUserId: String): Result<Unit> {
        return withContext(dispatcher) {
            networkUserRepository.followUser(userId, targetUserId)
        }
    }

    override suspend fun unfollowUser(userId: String, targetUserId: String): Result<Unit> {
        return withContext(dispatcher) {
            networkUserRepository.unfollowUser(userId, targetUserId)
        }
    }

    override suspend fun getUserFollowing(userId: String): Result<List<User>> {
        return withContext(dispatcher) {
            networkUserRepository.getUserFollowing(userId)
        }
    }

    override suspend fun getUserFollowers(userId: String): Result<List<User>> {
        return withContext(dispatcher) {
            networkUserRepository.getUserFollowers(userId)
        }
    }

    override suspend fun checkFollowStatus(userId: String, targetUserId: String): Result<Boolean> {
        return withContext(dispatcher) {
            networkUserRepository.checkFollowStatus(userId, targetUserId)
        }
    }

    override suspend fun uploadAvatar(userId: String, imageBytes: ByteArray, fileName: String): Result<String> {
        return withContext(dispatcher) {
            try {
                val result = networkUserRepository.uploadAvatar(userId, imageBytes, fileName)
                result.onSuccess { avatarUrl ->
                    // 上传成功后，更新本地数据库中的头像URL
                    // 这里需要先获取用户信息，然后更新
                    val existingUser = userDao.getUserByUsername(userId) // 假设userId就是username
                    existingUser?.let { user ->
                        val updatedUser = user.copy(avatar = avatarUrl)
                        userDao.insertUser(updatedUser)
                    }
                }
                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun logout() {
        networkUserRepository.logout()
    }

    override fun isLoggedIn(): Boolean {
        return networkUserRepository.isLoggedIn()
    }
}