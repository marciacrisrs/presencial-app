package com.presencial.app.presentation.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.presencial.app.R
import com.presencial.app.presentation.dashboard.components.DashboardContent
import com.presencial.app.presentation.dashboard.components.DashboardSkeleton
import com.presencial.app.presentation.location.rememberWorkLocationPermissions

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    openCheckIn: Boolean = false,
    onCheckInHandled: () -> Unit = {},
    isHomeVisible: Boolean = true,
    onNavigateToWorkAddresses: () -> Unit = {}
) {
    val data by viewModel.dashboardData.collectAsStateWithLifecycle()
    val workAddresses by viewModel.workAddresses.collectAsStateWithLifecycle()
    val (foregroundPermissions, backgroundPermission) = rememberWorkLocationPermissions()
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val checkInSuccessMessage = stringResource(R.string.dashboard_check_in_success)
    val checkInRemovedMessage = stringResource(R.string.dashboard_check_in_removed)
    val yesterdaySuccessMessage = stringResource(R.string.dashboard_yesterday_check_in_success)

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            val message = when (event) {
                DashboardUiEvent.CheckInRegistered -> checkInSuccessMessage
                DashboardUiEvent.CheckInRemoved -> checkInRemovedMessage
                DashboardUiEvent.YesterdayCheckInRegistered -> yesterdaySuccessMessage
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(openCheckIn, isHomeVisible) {
        if (openCheckIn && isHomeVisible) onCheckInHandled()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (data == null) {
            DashboardSkeleton()
            return@Scaffold
        }

        val dashboard = data!!

        Box(modifier = Modifier.padding(padding)) {
            DashboardContent(
                dashboard = dashboard,
                activeWorkAddressCount = workAddresses.count { it.isActive },
                foregroundGranted = foregroundPermissions.allPermissionsGranted,
                backgroundGranted = backgroundPermission.allPermissionsGranted,
                onToggleTodayCheckIn = { viewModel.toggleTodayCheckIn(!dashboard.todayIsPresencial) },
                onMarkYesterdayPresencial = viewModel::markYesterdayPresencial,
                onNavigateToWorkAddresses = onNavigateToWorkAddresses,
                haptic = haptic,
                scrollToActions = openCheckIn
            )
        }
    }
}
