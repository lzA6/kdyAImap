package com.example.kdyaimap.core.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit

/**
 * Retrofit客户端单例
 * 提供网络请求配置和API服务实例
 */
object RetrofitClient {
    
    // ！！！请将下面的URL替换为您自己的Cloudflare Worker地址！！！
    private const val BASE_URL = "https://my-map-backend.tfai.workers.dev/" // 替换为实际的API基础URL
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L
    
    private var authToken: String? = null
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        authToken?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        chain.proceed(requestBuilder.build())
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    
    // 配置自定义的Gson实例
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(com.example.kdyaimap.core.model.User::class.java, UserTypeAdapter())
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
    
    /**
     * 设置认证Token
     */
    fun setAuthToken(token: String) {
        authToken = token
    }
    
    /**
     * 清除认证Token
     */
    fun clearAuthToken() {
        authToken = null
    }
    
    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean {
        return authToken != null
    }
    
    /**
     * 获取当前Token
     */
    fun getCurrentToken(): String? = authToken
}