package com.example.kdyaimap.core.data.network

import com.example.kdyaimap.core.data.di.NetworkConfig
import com.example.kdyaimap.core.data.network.model.PoiDto
import com.example.kdyaimap.core.data.network.model.PointDto
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 故障转移网络仓库
 * 当主API不可用时，自动切换到备用方案
 */
@Singleton
class FailoverNetworkRepository @Inject constructor(
    private val primaryRepository: NetworkEventRepository
) {
    
    private val TAG = "FailoverNetworkRepo"
    
    /**
     * 获取POI数据（带故障转移）
     */
    suspend fun getPoisWithFailover(category: String? = null): Result<List<PoiDto>> {
        // 首先尝试主API
        val primaryResult = primaryRepository.getPois(category)
        if (primaryResult.isSuccess) {
            return primaryResult
        }
        
        // 主API失败，记录错误
        val error = primaryResult.exceptionOrNull()
        android.util.Log.w(TAG, "主API连接失败: ${error?.message}")
        
        // 尝试备用方案1：返回模拟数据
        return getMockPois(category)
    }
    
    /**
     * 获取模拟POI数据（用于网络不可用时的备用方案）
     */
    private suspend fun getMockPois(category: String? = null): Result<List<PoiDto>> {
        android.util.Log.i(TAG, "使用模拟POI数据")
        
        val mockPois = listOf(
            PoiDto(
                id = 1L,
                name = "北京大学",
                latitude = 39.9990,
                longitude = 116.3161,
                category = "教育",
                description = "中国著名高等学府",
                createdAt = null
            ),
            PoiDto(
                id = 2L,
                name = "清华大学",
                latitude = 40.0042,
                longitude = 116.3261,
                category = "教育",
                description = "中国顶尖工程技术大学",
                createdAt = null
            ),
            PoiDto(
                id = 3L,
                name = "天安门广场",
                latitude = 39.9055,
                longitude = 116.3976,
                category = "景点",
                description = "北京市中心的著名广场",
                createdAt = null
            ),
            PoiDto(
                id = 4L,
                name = "故宫博物院",
                latitude = 39.9163,
                longitude = 116.3972,
                category = "景点",
                description = "明清两代的皇家宫殿",
                createdAt = null
            ),
            PoiDto(
                id = 5L,
                name = "王府井大街",
                latitude = 39.9139,
                longitude = 116.4107,
                category = "购物",
                description = "北京著名商业街",
                createdAt = null
            )
        )
        
        // 如果指定了分类，进行过滤
        val filteredPois = if (category != null) {
            mockPois.filter { it.category.contains(category, ignoreCase = true) }
        } else {
            mockPois
        }
        
        return Result.success(filteredPois)
    }
    
    /**
     * 测试网络连接状态
     */
    suspend fun testNetworkConnectivity(): NetworkStatus {
        return try {
            val result = primaryRepository.getPois()
            if (result.isSuccess) {
                NetworkStatus.CONNECTED
            } else {
                NetworkStatus.FAILOVER_MODE
            }
        } catch (e: Exception) {
            NetworkStatus.DISCONNECTED
        }
    }
}

/**
 * 网络状态枚举
 */
enum class NetworkStatus {
    CONNECTED,           // 正常连接
    FAILOVER_MODE,      // 故障转移模式
    DISCONNECTED         // 完全断开连接
}