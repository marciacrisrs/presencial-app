package com.presencial.app.presentation.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.presencial.app.presentation.dashboard.components.DashboardContent
import com.presencial.app.presentation.dashboard.components.DashboardSkeleton
import com.presencial.app.presentation.location.rememberWorkLocationPermissions

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    openCheckIn: Boolean = false,
    onCheckInHandled: () -> Unit = {}
) {
    val data by viewModel.dashboardData.collectAsStateWithLifecycle()
    val workAddresses by viewModel.workAddresses.collectAsStateWithLifecycle()
    val (foregroundPermissions, backgroundPermission) = rememberWorkLocationPermissions()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(openCheckIn) {
        if (openCheckIn) onCheckInHandled()
    }

    if (data == null) {
        DashboardSkeleton()
        return
    }

    val dashboard = data!!

    DashboardContent(
        dashboard = dashboard,
        activeWorkAddressCount = workAddresses.count { it.isActive },
        foregroundGranted = foregroundPermissions.allPermissionsGranted,
        backgroundGranted = backgroundPermission.allPermissionsGranted,
        onToggleTodayCheckIn = { viewModel.toggleTodayCheckIn(true) },
        onMarkYesterdayPresencial = viewModel::markYesterdayPresencial,
        haptic = haptic
    )
}
