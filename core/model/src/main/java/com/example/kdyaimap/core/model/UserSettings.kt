package com.example.kdyaimap.core.model

/**
 * 用户设置数据类
 */
data class UserSettings(
    val eventNotifications: Boolean = true,
    val messageNotifications: Boolean = true,
    val systemNotifications: Boolean = true,
    val autoLocation: Boolean = true,
    val showNavigation: Boolean = true,
    val mapStyle: String = "normal", // normal, satellite, terrain
    val language: String = "zh-CN", // zh-CN, en-US
    val theme: String = "system" // light, dark, system
)