package com.example.kdyaimap.core.data.network

import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.core.model.User
import com.example.kdyaimap.core.model.PoiItem
import com.example.kdyaimap.core.data.network.model.PoiDto
import com.example.kdyaimap.core.data.network.model.PointDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API服务接口
 * 定义所有网络请求接口
 */
interface ApiService {
    
    // ==================== 用户相关接口 ====================
    
    /**
     * 用户登录
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>
    
    /**
     * 用户注册
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<User>>
    
    /**
     * 根据ID获取用户信息
     */
    @GET("users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): Response<ApiResponse<User>>
    
    /**
     * 更新用户信息
     */
    @PUT("users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: String,
        @Body request: UpdateUserRequest
    ): Response<ApiResponse<User>>
    
    /**
     * 获取所有用户
     */
    @GET("users")
    suspend fun getAllUsers(): Response<ApiResponse<List<User>>>
    
    /**
     * 关注用户
     */
    @POST("users/{userId}/follow/{targetUserId}")
    suspend fun followUser(
        @Path("userId") userId: String,
        @Path("targetUserId") targetUserId: String
    ): Response<ApiResponse<Unit>>
    
    /**
     * 取消关注
     */
    @DELETE("users/{userId}/follow/{targetUserId}")
    suspend fun unfollowUser(
        @Path("userId") userId: String,
        @Path("targetUserId") targetUserId: String
    ): Response<ApiResponse<Unit>>
    
    /**
     * 获取用户关注列表
     */
    @GET("users/{userId}/following")
    suspend fun getUserFollowing(@Path("userId") userId: String): Response<ApiResponse<List<User>>>
    
    /**
     * 获取用户粉丝列表
     */
    @GET("users/{userId}/followers")
    suspend fun getUserFollowers(@Path("userId") userId: String): Response<ApiResponse<List<User>>>
    
    /**
     * 检查关注状态
     */
    @GET("users/{userId}/follow/{targetUserId}")
    suspend fun checkFollowStatus(
        @Path("userId") userId: String,
        @Path("targetUserId") targetUserId: String
    ): Response<ApiResponse<Boolean>>
    
    /**
     * 上传头像
     */
    @Multipart
    @POST("users/{userId}/avatar")
    suspend fun uploadAvatar(
        @Path("userId") userId: String,
        @Part avatar: MultipartBody.Part
    ): Response<ApiResponse<String>>
    
    // ==================== POI相关接口 ====================
    
    /**
     * 获取地图上的所有兴趣点 (POIs)
     * 对应后端的 GET /api/points 接口
     * @param category 可选的查询参数，用于按类型筛选，例如 "FOOD"
     * @return 返回一个 PoiDto 对象的列表
     */
    @GET("api/points")
    suspend fun getPois(@Query("category") category: String? = null): List<PointDto>
    
    /**
     * 创建新的POI
     * 对应后端的 POST /api/points 接口
     */
    @POST("api/points")
    suspend fun createPoi(@Body pointDto: PointDto): Response<PointDto>
    
    /**
     * 更新POI
     * 对应后端的 PUT /api/points/{id} 接口
     */
    @PUT("api/points/{id}")
    suspend fun updatePoi(
        @Path("id") id: Long,
        @Body pointDto: PointDto
    ): Response<PointDto>
    
    /**
     * 删除POI
     * 对应后端的 DELETE /api/points/{id} 接口
     */
    @DELETE("api/points/{id}")
    suspend fun deletePoi(@Path("id") id: Long): Response<Unit>
    
    // ==================== 活动相关接口 ====================
    
    /**
     * 获取所有活动
     */
    @GET("events")
    suspend fun getAllEvents(): Response<ApiResponse<List<CampusEvent>>>
    
    /**
     * 根据ID获取活动
     */
    @GET("events/{eventId}")
    suspend fun getEventById(@Path("eventId") eventId: String): Response<ApiResponse<CampusEvent>>
    
    /**
     * 创建活动
     */
    @POST("events")
    suspend fun createEvent(@Body request: CreateEventRequest): Response<ApiResponse<CampusEvent>>
    
    /**
     * 更新活动
     */
    @PUT("events/{eventId}")
    suspend fun updateEvent(
        @Path("eventId") eventId: String,
        @Body request: UpdateEventRequest
    ): Response<ApiResponse<CampusEvent>>
    
    /**
     * 删除活动
     */
    @DELETE("events/{eventId}")
    suspend fun deleteEvent(@Path("eventId") eventId: String): Response<ApiResponse<Unit>>
    
    /**
     * 根据类型获取活动
     */
    @GET("events/type/{eventType}")
    suspend fun getEventsByType(@Path("eventType") eventType: String): Response<ApiResponse<List<CampusEvent>>>
    
    /**
     * 获取待审核活动
     */
    @GET("events/pending")
    suspend fun getPendingEvents(): Response<ApiResponse<List<CampusEvent>>>
    
    /**
     * 获取已审核活动
     */
    @GET("events/approved")
    suspend fun getApprovedEvents(): Response<ApiResponse<List<CampusEvent>>>
    
    /**
     * 上传活动图片
     */
    @Multipart
    @POST("events/{eventId}/images")
    suspend fun uploadEventImage(
        @Path("eventId") eventId: String,
        @Part image: MultipartBody.Part
    ): Response<ApiResponse<String>>
    
    // ==================== 请求/响应数据类 ====================
    
    data class LoginRequest(
        val username: String,
        val password: String
    )
    
    data class LoginResponse(
        val user: User,
        val token: String
    )
    
    data class RegisterRequest(
        val username: String,
        val password: String,
        val email: String,
        val role: String = "STUDENT"
    )
    
    data class UpdateUserRequest(
        val username: String? = null,
        val email: String? = null,
        val avatar: String? = null,
        val bio: String? = null,
        val phone: String? = null
    )
    
    data class CreateEventRequest(
        val title: String,
        val description: String,
        val eventType: String,
        val location: String,
        val latitude: Double,
        val longitude: Double,
        val startTime: Long,
        val endTime: Long,
        val maxParticipants: Int? = null,
        val images: List<String>? = null,
        val organizerId: String
    )
    
    data class UpdateEventRequest(
        val title: String? = null,
        val description: String? = null,
        val eventType: String? = null,
        val location: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val startTime: Long? = null,
        val endTime: Long? = null,
        val maxParticipants: Int? = null,
        val images: List<String>? = null,
        val status: String? = null
    )
    
    data class ApiResponse<T>(
        val success: Boolean,
        val data: T? = null,
        val message: String? = null,
        val code: Int? = null
    )
}