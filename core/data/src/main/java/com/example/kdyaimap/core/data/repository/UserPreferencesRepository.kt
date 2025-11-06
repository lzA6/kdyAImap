package com.example.kdyaimap.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.kdyaimap.core.model.UserRole
import com.example.kdyaimap.core.model.UserSettings
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferencesKeys {
        val USER_ID = longPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val USER_ROLE = stringPreferencesKey("user_role")
        val AVATAR = stringPreferencesKey("avatar")
        val BIO = stringPreferencesKey("bio")
        val EMAIL = stringPreferencesKey("email")
        val PHONE = stringPreferencesKey("phone")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val REMEMBER_LOGIN = booleanPreferencesKey("remember_login")
        val USER_SETTINGS = stringPreferencesKey("user_settings")
    }

    val userId: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_ID]
        }

    val username: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USERNAME]
        }

    val userRole: Flow<UserRole?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_ROLE]?.let { roleString ->
                try {
                    UserRole.valueOf(roleString)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
        }

    val rememberLogin: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.REMEMBER_LOGIN] ?: true
        }

    suspend fun saveUserInfo(user: com.example.kdyaimap.core.model.User, remember: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = user.id
            preferences[PreferencesKeys.USERNAME] = user.username
            preferences[PreferencesKeys.USER_ROLE] = user.role.name
            preferences[PreferencesKeys.AVATAR] = user.avatar
            preferences[PreferencesKeys.BIO] = user.bio
            preferences[PreferencesKeys.EMAIL] = user.email
            preferences[PreferencesKeys.PHONE] = user.phone
            preferences[PreferencesKeys.IS_LOGGED_IN] = true
            preferences[PreferencesKeys.REMEMBER_LOGIN] = remember
        }
    }

    suspend fun saveUserId(userId: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
        }
    }

    suspend fun setRememberLogin(remember: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMEMBER_LOGIN] = remember
        }
    }

    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = false
            // 如果不记住登录，则清除所有用户信息
            if (!(preferences[PreferencesKeys.REMEMBER_LOGIN] ?: true)) {
                preferences.clear()
            }
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // ==================== 用户设置相关 ====================

    val userSettings: Flow<UserSettings> = context.dataStore.data
        .map { preferences ->
            val settingsJson = preferences[PreferencesKeys.USER_SETTINGS]
            if (settingsJson != null) {
                try {
                    Gson().fromJson(settingsJson, UserSettings::class.java)
                } catch (e: Exception) {
                    UserSettings() // 解析失败时返回默认设置
                }
            } else {
                UserSettings() // 没有设置时返回默认设置
            }
        }

    suspend fun updateUserSettings(settings: UserSettings) {
        context.dataStore.edit { preferences ->
            val settingsJson = Gson().toJson(settings)
            preferences[PreferencesKeys.USER_SETTINGS] = settingsJson
        }
    }

    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = 0L
            preferences[PreferencesKeys.USERNAME] = ""
            preferences[PreferencesKeys.USER_ROLE] = ""
            preferences[PreferencesKeys.AVATAR] = ""
            preferences[PreferencesKeys.BIO] = ""
            preferences[PreferencesKeys.EMAIL] = ""
            preferences[PreferencesKeys.PHONE] = ""
            preferences[PreferencesKeys.IS_LOGGED_IN] = false
            // 保留设置和记住登录状态
        }
    }

    suspend fun clearCache() {
        // 这里可以添加清除缓存的逻辑
        // 比如清除图片缓存、临时文件等
    }
}