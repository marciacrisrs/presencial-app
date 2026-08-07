package com.presencial.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = SHIMMER_INITIAL_VALUE,
        targetValue = SHIMMER_TARGET_VALUE,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SHIMMER_DURATION_MILLIS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = SHIMMER_COLOR_ALPHA_HIGH),
        Color.LightGray.copy(alpha = SHIMMER_COLOR_ALPHA_LOW),
        Color.LightGray.copy(alpha = SHIMMER_COLOR_ALPHA_HIGH),
    )

    this.background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnim, y = translateAnim)
        )
    )
}

@Composable
fun ShimmerBox(
    height: Dp,
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f,
    shape: RoundedCornerShape = RoundedCornerShape(SHIMMER_BOX_CORNER_RADIUS.dp)
) {
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .background(Color.LightGray.copy(alpha = SHIMMER_BOX_ALPHA), shape)
            .shimmer()
    )
}

private const val SHIMMER_INITIAL_VALUE = 0f
private const val SHIMMER_TARGET_VALUE = 1000f
private const val SHIMMER_DURATION_MILLIS = 1200
private const val SHIMMER_COLOR_ALPHA_HIGH = 0.6f
private const val SHIMMER_COLOR_ALPHA_LOW = 0.2f
private const val SHIMMER_BOX_ALPHA = 0.1f
private const val SHIMMER_BOX_CORNER_RADIUS = 8

