package com.presencial.app.presentation.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
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
    haptic: HapticFeedback
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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

        DashboardStats(dashboard)

        DashboardProgressBarSection(dashboard)

        Spacer(modifier = Modifier.weight(1f))

        DashboardActionSection(
            dashboard = dashboard,
            onToggleTodayCheckIn = onToggleTodayCheckIn,
            onMarkYesterdayPresencial = onMarkYesterdayPresencial,
            haptic = haptic
        )
    }
}

private val SCREEN_PADDING = 16.dp
private val CONTENT_SPACING = 8.dp
