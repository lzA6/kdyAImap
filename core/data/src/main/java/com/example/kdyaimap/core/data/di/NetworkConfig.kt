package com.example.kdyaimap.core.data.di

/**
 * 网络配置类
 * 提供多种网络连接方案
 */
object NetworkConfig {
    
    // 主要API地址（Cloudflare Workers - 可能需要代理）
    const val PRIMARY_BASE_URL = "https://my-map-backend.tfai.workers.dev/"
    
    // 备用API地址选项（国内可访问的服务）
    const val BACKUP_BASE_URL_1 = "https://api.github.com/" // GitHub API（国内可访问）
    const val BACKUP_BASE_URL_2 = "https://httpbin.org/" // 测试用API
    const val BACKUP_BASE_URL_3 = "https://jsonplaceholder.typicode.com/" // 测试用API
    
    // 代理配置
    data class ProxyConfig(
        val enabled: Boolean = false,
        val host: String = "",
        val port: Int = 8080,
        val username: String? = null,
        val password: String? = null
    )
    
    // 当前代理配置
    val currentProxyConfig = ProxyConfig(
        enabled = false, // 设置为true启用代理
        host = "127.0.0.1", // 代理服务器地址
        port = 7890, // 代理服务器端口（常见代理端口：7890, 1080, 8080）
        username = null, // 代理用户名（如果需要）
        password = null // 代理密码（如果需要）
    )
    
    // 网络超时配置
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L
    
    // 重试配置
    const val MAX_RETRIES = 3
    const val RETRY_DELAY_MS = 1000L
}