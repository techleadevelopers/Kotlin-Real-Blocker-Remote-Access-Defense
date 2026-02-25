package com.blockremote.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.blockremote.ui.screens.AppShieldScreen
import com.blockremote.ui.screens.AuditLogsScreen
import com.blockremote.ui.screens.DashboardScreen
import com.blockremote.ui.screens.SensorMonitorScreen
import com.blockremote.ui.screens.SettingsScreen
import com.blockremote.viewmodel.BlockRemoteViewModel

sealed class Screen(val route: String, val label: String, val icon: String) {
    object Dashboard : Screen("dashboard", "Dashboard", "shield")
    object SensorMonitor : Screen("sensors", "Sensors", "radar")
    object AppShield : Screen("shield", "Shield", "lock")
    object AuditLogs : Screen("logs", "Logs", "terminal")
    object Settings : Screen("settings", "Config", "settings")
}

val screens = listOf(
    Screen.Dashboard,
    Screen.SensorMonitor,
    Screen.AppShield,
    Screen.AuditLogs,
    Screen.Settings
)

@Composable
fun BlockRemoteNavGraph(
    navController: NavHostController,
    viewModel: BlockRemoteViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        }
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(viewModel)
        }
        composable(Screen.SensorMonitor.route) {
            SensorMonitorScreen(viewModel)
        }
        composable(Screen.AppShield.route) {
            AppShieldScreen(viewModel)
        }
        composable(Screen.AuditLogs.route) {
            AuditLogsScreen(viewModel)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(viewModel)
        }
    }
}
