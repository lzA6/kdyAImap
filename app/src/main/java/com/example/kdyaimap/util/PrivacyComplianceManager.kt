package com.example.kdyaimap.util

import android.content.Context
import android.util.Log
import com.amap.api.maps.AMap
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.MyLocationStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PrivacyComplianceManager {
    private const val TAG = "PrivacyComplianceManager"
    private var isInitialized = false
    
    suspend fun initializePrivacyCompliance(context: Context) = withContext(Dispatchers.IO) {
        if (isInitialized) {
            Log.d(TAG, "隐私合规已初始化")
            return@withContext
        }
        
        try {
            // 高德地图隐私合规配置
            withContext(Dispatchers.Main) {
                // 更新隐私合规设置
                MapsInitializer.updatePrivacyShow(context, true, true)
                MapsInitializer.updatePrivacyAgree(context, true)
                
                // 初始化地图SDK
                MapsInitializer.initialize(context)
            }
            
            isInitialized = true
            Log.i(TAG, "隐私合规初始化成功")
            
        } catch (e: Exception) {
            Log.e(TAG, "隐私合规初始化失败", e)
            throw e
        }
    }
    
    fun configureMapPrivacy(aMap: AMap) {
        try {
            // 配置地图隐私设置
            val myLocationStyle = MyLocationStyle()
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
            myLocationStyle.interval(2000)
            aMap.myLocationStyle = myLocationStyle
            aMap.isMyLocationEnabled = true
            
            Log.d(TAG, "地图隐私配置完成")
            
        } catch (e: Exception) {
            Log.e(TAG, "地图隐私配置失败", e)
        }
    }
    
    fun isPrivacyCompliant(): Boolean = isInitialized
    
    suspend fun resetPrivacyCompliance(context: Context) = withContext(Dispatchers.IO) {
        try {
            isInitialized = false
            initializePrivacyCompliance(context)
            
        } catch (e: Exception) {
            Log.e(TAG, "重置隐私合规失败", e)
            throw e
        }
    }
}