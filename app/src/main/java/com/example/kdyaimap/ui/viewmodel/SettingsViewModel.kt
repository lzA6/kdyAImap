package com.example.kdyaimap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kdyaimap.core.data.repository.UserPreferencesRepository
import com.example.kdyaimap.core.model.UserSettings
import com.example.kdyaimap.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _settingsState = MutableStateFlow<SettingsState>(SettingsState.Loading)
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * 加载设置
     */
    fun loadSettings() {
        viewModelScope.launch {
            _settingsState.value = SettingsState.Loading
            
            try {
                val settings = userPreferencesRepository.userSettings.first()
                _settingsState.value = SettingsState.Success(settings)
            } catch (e: Exception) {
                _settingsState.value = SettingsState.Error("加载设置失败：${e.message}")
            }
        }
    }

    /**
     * 更新活动通知设置
     */
    fun updateEventNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = (_settingsState.value as? SettingsState.Success)?.settings
                    ?: UserSettings()
                val newSettings = currentSettings.copy(eventNotifications = enabled)
                userPreferencesRepository.updateUserSettings(newSettings)
                _settingsState.value = SettingsState.Success(newSettings)
            } catch (e: Exception) {
                // 可以在这里处理错误
            }
        }
    }

    /**
     * 更新消息通知设置
     */
    fun updateMessageNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = (_settingsState.value as? SettingsState.Success)?.settings
                    ?: UserSettings()
                val newSettings = currentSettings.copy(messageNotifications = enabled)
                userPreferencesRepository.updateUserSettings(newSettings)
                _settingsState.value = SettingsState.Success(newSettings)
            } catch (e: Exception) {
                // 可以在这里处理错误
            }
        }
    }

    /**
     * 更新系统通知设置
     */
    fun updateSystemNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = (_settingsState.value as? SettingsState.Success)?.settings
                    ?: UserSettings()
                val newSettings = currentSettings.copy(systemNotifications = enabled)
                userPreferencesRepository.updateUserSettings(newSettings)
                _settingsState.value = SettingsState.Success(newSettings)
            } catch (e: Exception) {
                // 可以在这里处理错误
            }
        }
    }

    /**
     * 更新自动定位设置
     */
    fun updateAutoLocation(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = (_settingsState.value as? SettingsState.Success)?.settings
                    ?: UserSettings()
                val newSettings = currentSettings.copy(autoLocation = enabled)
                userPreferencesRepository.updateUserSettings(newSettings)
                _settingsState.value = SettingsState.Success(newSettings)
            } catch (e: Exception) {
                // 可以在这里处理错误
            }
        }
    }

    /**
     * 更新显示导航设置
     */
    fun updateShowNavigation(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = (_settingsState.value as? SettingsState.Success)?.settings
                    ?: UserSettings()
                val newSettings = currentSettings.copy(showNavigation = enabled)
                userPreferencesRepository.updateUserSettings(newSettings)
                _settingsState.value = SettingsState.Success(newSettings)
            } catch (e: Exception) {
                // 可以在这里处理错误
            }
        }
    }

    /**
     * 清除缓存
     */
    fun clearCache() {
        viewModelScope.launch {
            try {
                // 这里可以添加清除缓存的逻辑
                // 比如清除图片缓存、数据库缓存等
                userPreferencesRepository.clearCache()
            } catch (e: Exception) {
                // 可以在这里处理错误
            }
        }
    }

    /**
     * 退出登录
     */
    fun logout() {
        viewModelScope.launch {
            try {
                userRepository.logout()
                userPreferencesRepository.clearUserData()
                _settingsState.value = SettingsState.Error("用户已登出")
            } catch (e: Exception) {
                _settingsState.value = SettingsState.Error("退出登录失败：${e.message}")
            }
        }
    }
}


sealed class SettingsState {
    object Loading : SettingsState()
    data class Success(val settings: UserSettings) : SettingsState()
    data class Error(val message: String) : SettingsState()
}