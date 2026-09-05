package com.presencial.app.presentation.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.unit.Dp
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
    onNavigateToWorkAddresses: () -> Unit,
    haptic: HapticFeedback,
    scrollToActions: Boolean = false
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(scrollToActions) {
        if (scrollToActions && scrollState.maxValue > 0) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val layout = dashboardLayoutFor(maxHeight)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .heightIn(min = maxHeight)
                .padding(layout.screenPadding),
            verticalArrangement = Arrangement.spacedBy(layout.contentSpacing)
        ) {
            DashboardHeader(
                dashboard = dashboard,
                logoSize = layout.logoSize
            )

            MonitoringStatusBanner(
                activeAddressCount = activeWorkAddressCount,
                foregroundGranted = foregroundGranted,
                backgroundGranted = backgroundGranted,
                onClick = onNavigateToWorkAddresses
            )

            DashboardSmartMessageSection(dashboard)

            DashboardProgressSection(
                dashboard = dashboard,
                ringSize = layout.ringSize,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = layout.progressMinHeight)
            )

            DashboardActionSection(
                dashboard = dashboard,
                onToggleTodayCheckIn = onToggleTodayCheckIn,
                onMarkYesterdayPresencial = onMarkYesterdayPresencial,
                haptic = haptic,
                buttonHeight = layout.buttonHeight
            )
        }
    }
}

private fun dashboardLayoutFor(maxHeight: Dp): DashboardLayout {
    val compact = maxHeight < COMPACT_HEIGHT
    val screenPadding = if (compact) PADDING_COMPACT else PADDING_COMFORTABLE
    val contentSpacing = if (compact) SPACING_COMPACT else SPACING_COMFORTABLE
    val logoSize = if (compact) LOGO_SIZE_COMPACT else LOGO_SIZE_COMFORTABLE
    val buttonHeight = if (compact) BUTTON_HEIGHT_COMPACT else BUTTON_HEIGHT_COMFORTABLE
    val reserved = screenPadding * 2 +
        logoSize +
        SMART_MESSAGE_RESERVE +
        buttonHeight +
        contentSpacing * SPACING_SLOTS +
        EXTRA_SECTION_SLACK
    val progressMinHeight = (maxHeight - reserved).coerceAtLeast(
        if (compact) PROGRESS_MIN_COMPACT else PROGRESS_MIN_REGULAR
    )
    val ringSize = (progressMinHeight * RING_FRACTION).coerceIn(RING_SIZE_MIN, RING_SIZE_MAX)
    return DashboardLayout(
        screenPadding = screenPadding,
        contentSpacing = contentSpacing,
        logoSize = logoSize,
        ringSize = ringSize,
        progressMinHeight = progressMinHeight,
        buttonHeight = buttonHeight
    )
}

private data class DashboardLayout(
    val screenPadding: Dp,
    val contentSpacing: Dp,
    val logoSize: Dp,
    val ringSize: Dp,
    val progressMinHeight: Dp,
    val buttonHeight: Dp
)

private val COMPACT_HEIGHT = 600.dp
private val PADDING_COMPACT = 16.dp
private val PADDING_COMFORTABLE = 24.dp
private val SPACING_COMPACT = 12.dp
private val SPACING_COMFORTABLE = 20.dp
private val LOGO_SIZE_COMPACT = 40.dp
private val LOGO_SIZE_COMFORTABLE = 48.dp
private val RING_SIZE_MIN = 140.dp
private val RING_SIZE_MAX = 260.dp
private val PROGRESS_MIN_COMPACT = 200.dp
private val PROGRESS_MIN_REGULAR = 240.dp
private val BUTTON_HEIGHT_COMPACT = 56.dp
private val BUTTON_HEIGHT_COMFORTABLE = 64.dp
private val SMART_MESSAGE_RESERVE = 80.dp
private val EXTRA_SECTION_SLACK = 24.dp
private const val SPACING_SLOTS = 4
private const val RING_FRACTION = 0.55f
