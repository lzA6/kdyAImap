package com.example.kdyaimap.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 网络状态管理器
 * 监控网络连接状态，提供实时的网络状态信息
 */
@Singleton
class NetworkStatusManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _isNetworkAvailable = MutableStateFlow(false)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()
    
    private val _networkType = MutableStateFlow(NetworkType.NONE)
    val networkType: StateFlow<NetworkType> = _networkType.asStateFlow()
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            updateNetworkStatus()
        }
        
        override fun onLost(network: Network) {
            super.onLost(network)
            updateNetworkStatus()
        }
        
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            updateNetworkStatus()
        }
    }
    
    init {
        registerNetworkCallback()
        updateNetworkStatus()
    }
    
    /**
     * 注册网络状态回调
     */
    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    
    /**
     * 更新网络状态
     */
    private fun updateNetworkStatus() {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        
        val isConnected = capabilities?.let {
            it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } ?: false
        
        _isNetworkAvailable.value = isConnected
        
        val type = when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.CELLULAR
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkType.ETHERNET
            else -> NetworkType.NONE
        }
        
        _networkType.value = type
    }
    
    /**
     * 检查网络是否可用
     */
    fun isNetworkAvailable(): Boolean {
        return _isNetworkAvailable.value
    }
    
    /**
     * 获取当前网络类型
     */
    fun getCurrentNetworkType(): NetworkType {
        return _networkType.value
    }
    
    /**
     * 取消注册网络回调
     */
    fun unregisterCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}

/**
 * 网络类型枚举
 */
enum class NetworkType {
    NONE,
    WIFI,
    CELLULAR,
    ETHERNET
}