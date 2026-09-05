package com.presencial.app.presentation.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.presencial.app.ui.components.ShimmerBox

@Composable
fun DashboardSkeleton() {
    Column(
        modifier = Modifier.fillMaxSize().padding(SCREEN_PADDING),
        verticalArrangement = Arrangement.spacedBy(CONTENT_SPACING)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HEADER_SPACING),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerBox(
                height = LOGO_SIZE,
                widthFraction = SHIMMER_LOGO_WIDTH,
                shape = RoundedCornerShape(SHIMMER_CORNER_RADIUS)
            )
            ShimmerBox(height = SHIMMER_TITLE_HEIGHT, widthFraction = SHIMMER_TITLE_WIDTH)
        }
        ShimmerBox(height = SHIMMER_SMART_MESSAGE_HEIGHT)
        ShimmerBox(
            height = SHIMMER_PROGRESS_HEIGHT,
            modifier = Modifier.weight(WEIGHT_EQUAL),
            shape = RoundedCornerShape(CORNER_RADIUS_EXTRA_LARGE)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(HEADER_SPACING)) {
            ShimmerBox(
                height = SHIMMER_STAT_HEIGHT,
                modifier = Modifier.weight(WEIGHT_EQUAL),
                shape = RoundedCornerShape(CORNER_RADIUS_LARGE)
            )
            ShimmerBox(
                height = SHIMMER_STAT_HEIGHT,
                modifier = Modifier.weight(WEIGHT_EQUAL),
                shape = RoundedCornerShape(CORNER_RADIUS_LARGE)
            )
            ShimmerBox(
                height = SHIMMER_STAT_HEIGHT,
                modifier = Modifier.weight(WEIGHT_EQUAL),
                shape = RoundedCornerShape(CORNER_RADIUS_LARGE)
            )
        }
        ShimmerBox(height = SHIMMER_BAR_HEIGHT)
    }
}

private val SCREEN_PADDING = 16.dp
private val CONTENT_SPACING = 16.dp
private val LOGO_SIZE = 40.dp
private val HEADER_SPACING = 8.dp
private val SHIMMER_TITLE_HEIGHT = 28.dp
private val SHIMMER_SMART_MESSAGE_HEIGHT = 72.dp
private val SHIMMER_PROGRESS_HEIGHT = 260.dp
private val SHIMMER_STAT_HEIGHT = 80.dp
private val SHIMMER_BAR_HEIGHT = 20.dp
private val CORNER_RADIUS_LARGE = 16.dp
private val CORNER_RADIUS_EXTRA_LARGE = 24.dp
private val SHIMMER_CORNER_RADIUS = 8.dp

private const val WEIGHT_EQUAL = 1f
private const val SHIMMER_LOGO_WIDTH = 0.1f
private const val SHIMMER_TITLE_WIDTH = 0.4f
