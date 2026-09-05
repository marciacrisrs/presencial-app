package com.presencial.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.presencial.app.R

@Composable
fun SmartMessageCard(
    message: String,
    modifier: Modifier = Modifier,
) {
    val messageDescription = stringResource(R.string.dashboard_message_content_description)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = messageDescription
            },
        shape = RoundedCornerShape(CORNER_RADIUS_MEDIUM),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = CARD_ALPHA_SECONDARY)
        )
    ) {
        Box(modifier = Modifier.padding(PADDING_LARGE).fillMaxWidth()) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(CORNER_RADIUS_LARGE),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = CARD_ALPHA_SURFACE)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(PADDING_MEDIUM),
            verticalArrangement = Arrangement.spacedBy(SPACING_TINY)
        ) {
            Text(
                text = title, 
                style = MaterialTheme.typography.labelMedium, 
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = ON_SURFACE_ALPHA_MEDIUM)
            )
            Text(text = value, style = MaterialTheme.typography.titleLarge)
            subtitle?.let {
                Text(
                    text = it, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = ON_SURFACE_ALPHA_LOW)
                )
            }
        }
    }
}

@Composable
fun CircularProgressCard(
    progress: Float,
    completedDays: Int,
    requiredDays: Int,
    remainingLine: String,
    policyLine: String,
    modifier: Modifier = Modifier,
    ringSize: Dp = CIRCLE_SIZE_DP
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(ANIM_DURATION_PROGRESS),
        label = "progress"
    )
    val daysCenter = stringResource(R.string.dashboard_days_center, completedDays, requiredDays)
    val daysCaption = stringResource(R.string.dashboard_days_center_caption)
    val strokeWidth = (ringSize.value / CIRCLE_SIZE_DP.value * STROKE_WIDTH_DP.value).dp
    val centerStyle = if (ringSize >= LARGE_RING_THRESHOLD) {
        MaterialTheme.typography.headlineMedium
    } else {
        MaterialTheme.typography.headlineSmall
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(CORNER_RADIUS_EXTRA_LARGE),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = CARD_ALPHA_PRIMARY)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PADDING_EXTRA_LARGE),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SPACING_MEDIUM, Alignment.CenterVertically)
        ) {
            Box(
                modifier = Modifier.size(ringSize),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().aspectRatio(ASPECT_RATIO_SQUARE),
                    strokeWidth = strokeWidth,
                    strokeCap = StrokeCap.Round,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = daysCenter,
                        style = centerStyle,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = daysCaption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = ON_SURFACE_ALPHA_MEDIUM),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Text(
                text = remainingLine,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Text(
                text = policyLine,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = ON_SURFACE_ALPHA_MEDIUM),
                textAlign = TextAlign.Center
            )
        }
    }
}

private const val ANIM_DURATION_PROGRESS = 800
private val CIRCLE_SIZE_DP = 180.dp
private val LARGE_RING_THRESHOLD = 200.dp
private const val CARD_ALPHA_SECONDARY = 0.5f
private const val CARD_ALPHA_SURFACE = 0.5f
private const val CARD_ALPHA_PRIMARY = 0.3f
private const val ON_SURFACE_ALPHA_MEDIUM = 0.7f
private const val ON_SURFACE_ALPHA_LOW = 0.6f
private val PADDING_MEDIUM = 12.dp
private val PADDING_LARGE = 16.dp
private val PADDING_EXTRA_LARGE = 24.dp
private val SPACING_TINY = 2.dp
private val SPACING_MEDIUM = 16.dp
private val CORNER_RADIUS_MEDIUM = 12.dp
private val CORNER_RADIUS_LARGE = 16.dp
private val CORNER_RADIUS_EXTRA_LARGE = 24.dp
private val STROKE_WIDTH_DP = 14.dp
private const val ASPECT_RATIO_SQUARE = 1f
