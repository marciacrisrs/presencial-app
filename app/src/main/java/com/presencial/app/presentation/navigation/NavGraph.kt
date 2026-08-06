package com.presencial.app.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.presencial.app.presentation.about.AboutScreen
import com.presencial.app.presentation.absence.AbsenceScreen
import com.presencial.app.presentation.calendar.CalendarScreen
import com.presencial.app.presentation.location.WorkAddressScreen
import com.presencial.app.presentation.dashboard.DashboardScreen
import com.presencial.app.presentation.history.HistoryScreen
import com.presencial.app.presentation.settings.SettingsScreen
import com.presencial.app.presentation.statistics.StatisticsScreen

@Composable
fun PresencialNavHost(
    openCheckIn: Boolean = false,
    onCheckInHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    if (openCheckIn && currentRoute != Screen.Dashboard.route) {
        navController.navigate(Screen.Dashboard.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
        }
        onCheckInHandled()
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Screen.bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                        alwaysShowLabel = false
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 300 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -300 }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -300 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 300 }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))
            }
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(openCheckIn = openCheckIn, onCheckInHandled = onCheckInHandled)
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(onNavigateToAbsences = { navController.navigate(Screen.Absences.route) })
            }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Statistics.route) { StatisticsScreen() }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToAbout = { navController.navigate(Screen.About.route) },
                    onNavigateToAbsences = { navController.navigate(Screen.Absences.route) },
                    onNavigateToWorkAddresses = { navController.navigate(Screen.WorkAddresses.route) }
                )
            }
            composable(Screen.About.route) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Absences.route) {
                AbsenceScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.WorkAddresses.route) {
                WorkAddressScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
