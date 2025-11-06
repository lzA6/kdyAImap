package com.example.kdyaimap.location

import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AMapLocationManager @Inject constructor(
    private val context: Context
) {
    private var locationClient: AMapLocationClient? = null
    private var locationOption: AMapLocationClientOption? = null
    private val _locationEvents = Channel<LocationEvent>(Channel.BUFFERED)
    
    val locationEvents: Flow<LocationEvent> = _locationEvents.receiveAsFlow()
    
    sealed class LocationEvent {
        data class LocationReceived(val location: AMapLocation) : LocationEvent()
        data class LocationError(val errorCode: Int, val errorInfo: String) : LocationEvent()
        object LocationPermissionDenied : LocationEvent()
    }
    
    init {
        initializeLocation()
    }
    
    private fun initializeLocation() {
        try {
            locationClient = AMapLocationClient(context).apply {
                setLocationOption(getDefaultLocationOption())
                setLocationListener(locationListener)
            }
        } catch (e: Exception) {
            _locationEvents.trySend(LocationEvent.LocationError(-1, "定位服务初始化失败: ${e.message}"))
        }
    }
    
    private fun getDefaultLocationOption(): AMapLocationClientOption {
        return AMapLocationClientOption().apply {
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            isOnceLocation = true
            isOnceLocationLatest = true
            interval = 2000
            isNeedAddress = true
            isMockEnable = false
            isLocationCacheEnable = true // 启用缓存以提高首次定位速度
            httpTimeOut = 30000 // 增加超时时间
            isSensorEnable = true // 启用传感器辅助定位
            isWifiScan = true
            isGpsFirst = false // 优先使用网络定位，更快获取位置
        }
    }
    
    private val locationListener = AMapLocationListener { location ->
        when {
            location.errorCode == 0 -> {
                // 定位成功
                if (location.latitude != 0.0 && location.longitude != 0.0) {
                    _locationEvents.trySend(LocationEvent.LocationReceived(location))
                } else {
                    _locationEvents.trySend(LocationEvent.LocationError(-1, "定位返回的坐标无效"))
                }
            }
            location.errorCode == 12 -> {
                _locationEvents.trySend(LocationEvent.LocationPermissionDenied)
            }
            location.errorCode == 4 -> {
                _locationEvents.trySend(LocationEvent.LocationError(location.errorCode, "网络异常，请检查网络连接"))
            }
            location.errorCode == 7 -> {
                _locationEvents.trySend(LocationEvent.LocationError(location.errorCode, "Key错误或权限不足，请检查API Key配置"))
            }
            location.errorCode == 8 -> {
                _locationEvents.trySend(LocationEvent.LocationError(location.errorCode, "请检查网络连接或重试"))
            }
            else -> {
                val errorMsg = when (location.errorCode) {
                    1 -> "定位失败，请检查定位服务是否开启"
                    2 -> "定位失败，请检查网络连接"
                    3 -> "定位失败，请检查定位权限"
                    5 -> "定位失败，请检查网络连接"
                    6 -> "定位失败，请检查定位服务"
                    9 -> "定位失败，请检查定位权限"
                    10 -> "定位失败，请检查网络连接"
                    11 -> "定位失败，请检查定位服务"
                    else -> "定位失败: ${location.errorInfo}"
                }
                _locationEvents.trySend(LocationEvent.LocationError(location.errorCode, errorMsg))
            }
        }
    }
    
    fun startLocation() {
        if (!hasLocationPermission()) {
            _locationEvents.trySend(LocationEvent.LocationPermissionDenied)
            return
        }
        
        try {
            locationClient?.startLocation()
        } catch (e: Exception) {
            _locationEvents.trySend(LocationEvent.LocationError(-1, "启动定位失败: ${e.message}"))
        }
    }
    
    fun stopLocation() {
        try {
            locationClient?.stopLocation()
        } catch (e: Exception) {
            _locationEvents.trySend(LocationEvent.LocationError(-1, "停止定位失败: ${e.message}"))
        }
    }
    
    fun startContinuousLocation() {
        if (!hasLocationPermission()) {
            _locationEvents.trySend(LocationEvent.LocationPermissionDenied)
            return
        }
        
        try {
            val option = AMapLocationClientOption().apply {
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                isOnceLocation = false
                interval = 5000
                isNeedAddress = true
                isMockEnable = false
                isLocationCacheEnable = false
                httpTimeOut = 20000
            }
            locationClient?.setLocationOption(option)
            locationClient?.startLocation()
        } catch (e: Exception) {
            _locationEvents.trySend(LocationEvent.LocationError(-1, "启动连续定位失败: ${e.message}"))
        }
    }
    
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun destroy() {
        try {
            locationClient?.stopLocation()
            locationClient?.onDestroy()
            locationClient = null
            _locationEvents.close()
        } catch (e: Exception) {
            // 忽略销毁时的异常
        }
    }
}