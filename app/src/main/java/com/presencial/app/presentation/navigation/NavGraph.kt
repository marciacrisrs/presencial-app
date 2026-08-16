package com.presencial.app.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.presencial.app.presentation.about.AboutScreen
import com.presencial.app.presentation.absence.AbsenceScreen
import com.presencial.app.presentation.calendar.CalendarScreen
import com.presencial.app.presentation.dashboard.DashboardScreen
import com.presencial.app.presentation.history.HistoryScreen
import com.presencial.app.presentation.location.WorkAddressScreen
import com.presencial.app.presentation.notification.RequestNotificationPermissionOnLaunch
import com.presencial.app.presentation.onboarding.OnboardingScreen
import com.presencial.app.presentation.onboarding.OnboardingViewModel
import com.presencial.app.presentation.settings.SettingsScreen
import com.presencial.app.presentation.statistics.StatisticsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val ANIM_DURATION = 400
private const val ANIM_OFFSET = 300

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PresencialNavHost(
    openCheckIn: Boolean = false,
    onCheckInHandled: () -> Unit = {},
    onboardingViewModel: OnboardingViewModel = hiltViewModel()
) {
    val showOnboarding = onboardingViewModel.uiState.collectAsStateWithLifecycle().value?.visible
    if (showOnboarding != false) {
        if (showOnboarding == true) {
            OnboardingNavHost(onboardingViewModel)
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val tabs = Screen.bottomNavItems
    val savedTab = rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = savedTab.intValue.coerceIn(0, tabs.lastIndex),
        pageCount = { tabs.size }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.settledPage) {
        savedTab.intValue = pagerState.settledPage
    }

    RequestNotificationPermissionOnLaunch(enabled = true)

    HandleCheckInNavigation(
        openCheckIn = openCheckIn,
        currentRoute = currentRoute,
        navController = navController,
        pagerState = pagerState,
        scope = scope,
        onCheckInHandled = onCheckInHandled
    )

    Scaffold(
        bottomBar = {
            if (Screen.isMainDestination(currentRoute)) {
                PresencialBottomBar(
                    selectedIndex = pagerState.currentPage,
                    onTabSelected = { index ->
                        scope.launch { pagerState.animateScrollToPage(index) }
                        navController.navigate(Screen.mainRoute(index)) {
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
    ) { padding ->
        PresencialNavGraph(
            navController = navController,
            padding = padding,
            pagerState = pagerState,
            openCheckIn = openCheckIn,
            onCheckInHandled = onCheckInHandled
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HandleCheckInNavigation(
    openCheckIn: Boolean,
    currentRoute: String?,
    navController: NavHostController,
    pagerState: PagerState,
    scope: CoroutineScope,
    onCheckInHandled: () -> Unit
) {
    if (openCheckIn && (pagerState.currentPage != 0 || !Screen.isMainDestination(currentRoute))) {
        scope.launch { pagerState.animateScrollToPage(0) }
        navController.navigate(Screen.mainRoute(0)) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
        onCheckInHandled()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PresencialNavGraph(
    navController: NavHostController,
    padding: androidx.compose.foundation.layout.PaddingValues,
    pagerState: PagerState,
    openCheckIn: Boolean,
    onCheckInHandled: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.mainRoute(0),
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
        composable(
            route = "${Screen.MAIN_ROUTE}?${Screen.TAB_ARG}={tab}",
            arguments = listOf(
                navArgument(Screen.TAB_ARG) {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val tab = backStackEntry.arguments?.getInt(Screen.TAB_ARG) ?: 0
            LaunchedEffect(tab) {
                if (pagerState.currentPage != tab) {
                    pagerState.scrollToPage(tab)
                }
            }
            MainTabPager(
                pagerState = pagerState,
                navController = navController,
                openCheckIn = openCheckIn,
                onCheckInHandled = onCheckInHandled
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainTabPager(
    pagerState: PagerState,
    navController: NavHostController,
    openCheckIn: Boolean,
    onCheckInHandled: () -> Unit
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        beyondViewportPageCount = 1
    ) { page ->
        when (page) {
            0 -> DashboardScreen(
                openCheckIn = openCheckIn,
                onCheckInHandled = onCheckInHandled
            )
            1 -> CalendarScreen(
                onNavigateToAbsences = { navController.navigate(Screen.Absences.route) }
            )
            2 -> HistoryScreen()
            3 -> StatisticsScreen()
            4 -> SettingsScreen(
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onNavigateToAbsences = { navController.navigate(Screen.Absences.route) },
                onNavigateToWorkAddresses = { navController.navigate(Screen.WorkAddresses.route) }
            )
        }
    }
}

@Composable
private fun PresencialBottomBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar {
        Screen.bottomNavItems.forEachIndexed { index, screen ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                alwaysShowLabel = false
            )
        }
    }
}

@Composable
private fun OnboardingNavHost(viewModel: OnboardingViewModel) {
    val navController = rememberNavController()
    Scaffold { padding ->
        NavHost(
            navController = navController,
            startDestination = ONBOARDING_ROUTE,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable(ONBOARDING_ROUTE) {
                OnboardingScreen(
                    viewModel = viewModel,
                    onAddWorkAddress = { navController.navigate(Screen.WorkAddresses.route) }
                )
            }
            composable(Screen.WorkAddresses.route) {
                WorkAddressScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

private const val ONBOARDING_ROUTE = "onboarding"
