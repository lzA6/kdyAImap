package com.example.kdyaimap.core.data.di

import com.example.kdyaimap.core.data.network.ApiService
import com.example.kdyaimap.core.data.network.NetworkEventRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetSocketAddress
import java.net.Proxy
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // [MODIFIED] 将 BASE_URL 修改为您的 Cloudflare Worker 地址
    private const val BASE_URL = "https://my-map-backend.tfai.workers.dev/"

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
        
        // 配置代理（如果需要）
        // 注意：这里提供一个示例代理配置，您需要根据实际情况修改
        try {
            // 如果需要使用代理，取消下面的注释并配置正确的代理地址
            /*
            val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("your-proxy-server.com", 8080))
            builder.proxy(proxy)
            */
        } catch (e: Exception) {
            // 代理配置失败时继续使用直连
        }
        
        return builder.build()
    }
    
    @Provides
    @Singleton
    fun provideNetworkEventRepository(apiService: ApiService): NetworkEventRepository {
        return NetworkEventRepository(apiService)
    }
}