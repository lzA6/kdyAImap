package com.example.kdyaimap.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "主页", Icons.Default.Home)
    object Map : Screen("map", "地图", Icons.Default.LocationOn)
    object Trading : Screen("trading", "二手市场", Icons.Default.ShoppingCart)
    object Notifications : Screen("notifications", "消息", Icons.Default.Notifications)
    object Profile : Screen("profile", "我的", Icons.Default.Person)
    object CreateEvent : Screen("create_event", "创建活动", Icons.Default.Event) // Not in bottom bar
    object EventDetail : Screen("event_detail/{eventId}", "活动详情", Icons.Default.Event) { // Not in bottom bar
        fun createRoute(eventId: Long) = "event_detail/$eventId"
    }
    object Admin : Screen("admin", "管理面板", Icons.Default.AdminPanelSettings) // Not in bottom bar
    object UserEvents : Screen("user_events", "我的活动", Icons.AutoMirrored.Filled.List) // Not in bottom bar
    object EditProfile : Screen("edit_profile", "编辑资料", Icons.Default.Edit) // Not in bottom bar
    object Settings : Screen("settings", "设置", Icons.Default.Settings) // Not in bottom bar
    object Followers : Screen("followers", "粉丝", Icons.Default.Group) // Not in bottom bar
    object Following : Screen("following", "关注", Icons.Default.Group) // Not in bottom bar
    
    // 二手交易相关路由
    object CreateTradingPost : Screen("create_trading_post", "发布商品", Icons.Default.Add) // Not in bottom bar
    object TradingPostDetail : Screen("trading_post_detail/{postId}", "商品详情", Icons.Default.ShoppingCart) { // Not in bottom bar
        fun createRoute(postId: Long) = "trading_post_detail/$postId"
    }
    
    // 私信相关路由
    object PrivateMessages : Screen("private_messages", "私信", Icons.AutoMirrored.Filled.Message) // Not in bottom bar
    object Chat : Screen("chat/{userId}", "聊天", Icons.AutoMirrored.Filled.Chat) { // Not in bottom bar
        fun createRoute(userId: Long) = "chat/$userId"
    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Trading,
    Screen.Map,
    Screen.Notifications,
    Screen.Profile
)