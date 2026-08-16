package com.presencial.app.presentation.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.unit.dp
import com.presencial.app.domain.model.DashboardData
import com.presencial.app.presentation.components.MonitoringStatusBanner

@Composable
fun DashboardContent(
    dashboard: DashboardData,
    activeWorkAddressCount: Int,
    foregroundGranted: Boolean,
    backgroundGranted: Boolean,
    onToggleTodayCheckIn: () -> Unit,
    onMarkYesterdayPresencial: () -> Unit,
    haptic: HapticFeedback,
    scrollToActions: Boolean = false
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(scrollToActions) {
        if (scrollToActions && scrollState.maxValue > 0) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(SCREEN_PADDING),
        verticalArrangement = Arrangement.spacedBy(CONTENT_SPACING)
    ) {
        DashboardHeader(dashboard)

        MonitoringStatusBanner(
            activeAddressCount = activeWorkAddressCount,
            foregroundGranted = foregroundGranted,
            backgroundGranted = backgroundGranted
        )

        DashboardSmartMessageSection(dashboard)

        DashboardProgressSection(dashboard)

        DashboardActionSection(
            dashboard = dashboard,
            onToggleTodayCheckIn = onToggleTodayCheckIn,
            onMarkYesterdayPresencial = onMarkYesterdayPresencial,
            haptic = haptic
        )

        Spacer(modifier = Modifier.height(BOTTOM_SPACING))
    }
}

private val SCREEN_PADDING = 16.dp
private val CONTENT_SPACING = 8.dp
private val BOTTOM_SPACING = 24.dp
