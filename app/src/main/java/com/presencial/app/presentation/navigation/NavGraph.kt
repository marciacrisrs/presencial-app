package com.presencial.app.presentation.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import com.presencial.app.presentation.notification.RequestNotificationPermissionOnLaunch
import com.presencial.app.presentation.settings.SettingsScreen
import com.presencial.app.presentation.statistics.StatisticsScreen

private const val ANIM_DURATION = 400
private const val ANIM_OFFSET = 300

@Composable
fun PresencialNavHost(
    openCheckIn: Boolean = false,
    onCheckInHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    RequestNotificationPermissionOnLaunch()

    HandleCheckInNavigation(
        openCheckIn = openCheckIn,
        currentRoute = currentRoute,
        navController = navController,
        onCheckInHandled = onCheckInHandled
    )

    Scaffold(
        bottomBar = {
            PresencialBottomBar(navController, currentRoute)
        }
    ) { padding ->
        PresencialNavGraph(
            navController = navController,
            padding = padding,
            openCheckIn = openCheckIn,
            onCheckInHandled = onCheckInHandled
        )
    }
}

@Composable
private fun HandleCheckInNavigation(
    openCheckIn: Boolean,
    currentRoute: String?,
    navController: androidx.navigation.NavHostController,
    onCheckInHandled: () -> Unit
) {
    if (openCheckIn && currentRoute != Screen.Dashboard.route) {
        navController.navigate(Screen.Dashboard.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
        }
        onCheckInHandled()
    }
}

@Composable
private fun PresencialNavGraph(
    navController: androidx.navigation.NavHostController,
    padding: androidx.compose.foundation.layout.PaddingValues,
    openCheckIn: Boolean,
    onCheckInHandled: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = Modifier.padding(padding),
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { ANIM_OFFSET }, 
                animationSpec = tween(ANIM_DURATION)
            ) + fadeIn(animationSpec = tween(ANIM_DURATION))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -ANIM_OFFSET }, 
                animationSpec = tween(ANIM_DURATION)
            ) + fadeOut(animationSpec = tween(ANIM_DURATION))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -ANIM_OFFSET }, 
                animationSpec = tween(ANIM_DURATION)
            ) + fadeIn(animationSpec = tween(ANIM_DURATION))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { ANIM_OFFSET }, 
                animationSpec = tween(ANIM_DURATION)
            ) + fadeOut(animationSpec = tween(ANIM_DURATION))
        }
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(openCheckIn = openCheckIn, onCheckInHandled = onCheckInHandled)
        }
        composable(Screen.Calendar.route) {
            CalendarScreen(onNavigateToAbsences = { 
                navController.navigate(Screen.Absences.route) 
            })
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

@Composable
private fun PresencialBottomBar(
    navController: androidx.navigation.NavHostController,
    currentRoute: String?
) {
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
