package com.example.kdyaimap.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.kdyaimap.ui.screens.AdminScreen
import com.example.kdyaimap.ui.screens.CreateEventScreen
import com.example.kdyaimap.ui.screens.EditProfileScreen
import com.example.kdyaimap.ui.screens.EventDetailScreen
import com.example.kdyaimap.ui.screens.HomeScreen
import com.example.kdyaimap.ui.screens.MapScreen
import com.example.kdyaimap.ui.screens.ProfileScreen
import com.example.kdyaimap.ui.screens.SettingsScreen
import com.example.kdyaimap.ui.screens.TradingScreen
import com.example.kdyaimap.ui.screens.CreateTradingPostScreen
import com.example.kdyaimap.ui.screens.TradingPostDetailScreen
import com.example.kdyaimap.ui.screens.NotificationScreen
import com.example.kdyaimap.ui.screens.UserEventsScreen
import com.example.kdyaimap.ui.screens.PrivateMessageScreen
import com.example.kdyaimap.ui.screens.ChatScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        floatingActionButton = {
            if (showBottomBar) {
                FloatingActionButton(onClick = { navController.navigate(Screen.CreateEvent.route) }) {
                    Icon(Icons.Default.Add, contentDescription = "创建活动")
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "mainGraph",
            modifier = Modifier.padding(innerPadding)
        ) {
            mainGraph(navController)
        }
    }
}

fun NavGraphBuilder.mainGraph(navController: NavHostController) {
    navigation(startDestination = Screen.Home.route, route = "mainGraph") {
        composable(Screen.Home.route) {
            HomeScreen(onEventClick = { eventId ->
                navController.navigate("${Screen.EventDetail.route.substringBefore("/{")}/$eventId")
            })
        }
        composable(Screen.Trading.route) {
            TradingScreen(
                onNavigateToCreatePost = { navController.navigate(Screen.CreateTradingPost.route) },
                onNavigateToPostDetail = { post ->
                    navController.navigate(Screen.TradingPostDetail.createRoute(post.id))
                }
            )
        }
        composable(Screen.Notifications.route) {
            NotificationScreen(
                onBack = { /* 消息页面不需要返回按钮，通过底部导航切换 */ }
            )
        }
        composable(Screen.Map.route) { MapScreen() }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToAdmin = { navController.navigate(Screen.Admin.route) },
                onNavigateToFollowers = { navController.navigate(Screen.Followers.route) },
                onNavigateToFollowing = { navController.navigate(Screen.Following.route) },
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToUserEvents = { navController.navigate(Screen.UserEvents.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.CreateTradingPost.route) {
            CreateTradingPostScreen(
                onBack = { navController.popBackStack() },
                onPostCreated = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.TradingPostDetail.route,
            arguments = listOf(navArgument("postId") { type = NavType.LongType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getLong("postId") ?: -1L
            TradingPostDetailScreen(
                postId = postId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.CreateEvent.route) {
            CreateEventScreen(
                onEventCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: -1L
            EventDetailScreen(
                eventId = eventId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Admin.route) {
            AdminScreen()
        }
        composable(Screen.UserEvents.route) {
            UserEventsScreen(
                onNavigateToEventDetail = { eventId ->
                    navController.navigate("${Screen.EventDetail.route.substringBefore("/{")}/$eventId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Followers.route) {
            // TODO: 实现粉丝页面
            Text("粉丝页面 - 开发中")
        }
        composable(Screen.Following.route) {
            // TODO: 实现关注页面
            Text("关注页面 - 开发中")
        }
        // 私信相关路由
        composable(Screen.PrivateMessages.route) {
            PrivateMessageScreen(
                onConversationClick = { conversation ->
                    navController.navigate(Screen.Chat.createRoute(userId = 1L)) // TODO: 使用实际的conversation ID
                },
                onFriendRequestClick = { /* 处理好友请求点击 */ }
            )
        }
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: -1L
            ChatScreen(
                conversationId = userId,
                participantName = "用户$userId",
                onBack = { navController.popBackStack() },
                onAddFriend = { /* 添加好友功能 */ }
            )
        }
    }
}