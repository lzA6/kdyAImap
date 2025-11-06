package com.example.kdyaimap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kdyaimap.core.data.network.FailoverNetworkRepository
import com.example.kdyaimap.core.data.network.NetworkStatus
import com.example.kdyaimap.core.data.di.NetworkConfig
import com.example.kdyaimap.util.NetworkTestHelper
import com.example.kdyaimap.util.NetworkDiagnosisReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 网络设置ViewModel
 * 管理网络设置和连接状态
 */
@HiltViewModel
class NetworkSettingsViewModel @Inject constructor(
    private val failoverRepository: FailoverNetworkRepository,
    private val networkTestHelper: NetworkTestHelper
) : ViewModel() {
    
    private val _networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.DISCONNECTED)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()
    
    private val _isTesting = MutableStateFlow(false)
    val isTesting: StateFlow<Boolean> = _isTesting.asStateFlow()
    
    private val _proxyEnabled = MutableStateFlow(NetworkConfig.currentProxyConfig.enabled)
    val proxyEnabled: StateFlow<Boolean> = _proxyEnabled.asStateFlow()
    
    private val _proxyHost = MutableStateFlow(NetworkConfig.currentProxyConfig.host)
    val proxyHost: StateFlow<String> = _proxyHost.asStateFlow()
    
    private val _proxyPort = MutableStateFlow(NetworkConfig.currentProxyConfig.port.toString())
    val proxyPort: StateFlow<String> = _proxyPort.asStateFlow()
    
    private val _diagnosisReport = MutableStateFlow<NetworkDiagnosisReport?>(null)
    val diagnosisReport: StateFlow<NetworkDiagnosisReport?> = _diagnosisReport.asStateFlow()
    
    private val _isDiagnosing = MutableStateFlow(false)
    val isDiagnosing: StateFlow<Boolean> = _isDiagnosing.asStateFlow()
    
    /**
     * 测试网络连接
     */
    fun testConnection() {
        viewModelScope.launch {
            _isTesting.value = true
            try {
                _networkStatus.value = failoverRepository.testNetworkConnectivity()
            } catch (e: Exception) {
                _networkStatus.value = NetworkStatus.DISCONNECTED
            } finally {
                _isTesting.value = false
            }
        }
    }
    
    /**
     * 更新代理启用状态
     */
    fun updateProxyEnabled(enabled: Boolean) {
        _proxyEnabled.value = enabled
    }
    
    /**
     * 更新代理主机
     */
    fun updateProxyHost(host: String) {
        _proxyHost.value = host
    }
    
    /**
     * 更新代理端口
     */
    fun updateProxyPort(port: String) {
        _proxyPort.value = port
    }
    
    /**
     * 运行网络诊断
     */
    fun runNetworkDiagnosis() {
        viewModelScope.launch {
            _isDiagnosing.value = true
            try {
                val report = networkTestHelper.runFullDiagnosis()
                _diagnosisReport.value = report
            } catch (e: Exception) {
                android.util.Log.e("NetworkSettingsViewModel", "网络诊断失败", e)
            } finally {
                _isDiagnosing.value = false
            }
        }
    }
}