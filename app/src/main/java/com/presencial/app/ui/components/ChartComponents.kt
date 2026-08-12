package com.presencial.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.presencial.app.R
import com.presencial.app.domain.model.MonthlySummary
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthlyBarChart(
    summaries: List<MonthlySummary>,
    modifier: Modifier = Modifier
) {
    if (summaries.isEmpty()) {
        Text(
            text = stringResource(R.string.chart_no_data),
            modifier = modifier.padding(16.dp)
        )
        return
    }

    val locale = Locale.getDefault()
    val monthSummaries = summaries.takeLast(MAX_VISIBLE_MONTHS).map { summary ->
        val month = summary.yearMonth.month.getDisplayName(TextStyle.SHORT, locale)
        stringResource(
            R.string.chart_month_summary,
            month,
            summary.achievedPercentage,
            summary.completedDays,
            summary.requiredDays
        )
    }
    val chartDescription = stringResource(
        R.string.chart_accessibility_summary,
        monthSummaries.joinToString(separator = ", ")
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CORNER_RADIUS_EXTRA_LARGE),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = ALPHA_SURFACE_VARIANT)
        )
    ) {
        Column(
            modifier = Modifier.padding(PADDING_EXTRA_LARGE),
            verticalArrangement = Arrangement.spacedBy(PADDING_MEDIUM)
        ) {
            Text(
                text = stringResource(R.string.chart_monthly_attendance_title),
                style = MaterialTheme.typography.titleLarge
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CHART_HEIGHT_DP)
                    .semantics { contentDescription = chartDescription }
            ) {
                val barWidth = size.width / (summaries.size * BAR_WIDTH_DIVISOR)
                val maxValue = summaries.maxOf { it.achievedPercentage }.coerceAtLeast(MAX_VALUE_DEFAULT)
                summaries.forEachIndexed { index, summary ->
                    val barHeight = (summary.achievedPercentage / maxValue) *
                        size.height * BAR_HEIGHT_FRACTION
                    val x = index * (barWidth * BAR_WIDTH_DIVISOR) + barWidth * BAR_WIDTH_OFFSET_FRACTION
                    val y = size.height - barHeight
                    drawRoundRect(
                        color = Color(COLOR_GREEN),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(CORNER_RADIUS_PX, CORNER_RADIUS_PX)
                    )
                }
            }
            monthSummaries.forEach { text ->
                Text(text = text, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun StatSummaryRow(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(CORNER_RADIUS_LARGE),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = ALPHA_SURFACE_VARIANT_LOW)
        )
    ) {
        Column(modifier = Modifier.padding(PADDING_LARGE)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = ALPHA_ON_SURFACE_MEDIUM)
            )
            Text(value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

private val CHART_HEIGHT_DP = 180.dp
private const val BAR_HEIGHT_FRACTION = 0.85f
private const val BAR_WIDTH_OFFSET_FRACTION = 0.5f
private const val COLOR_GREEN = 0xFF1B873B
private const val CORNER_RADIUS_PX = 8f
private const val MAX_VISIBLE_MONTHS = 6
private const val ALPHA_SURFACE_VARIANT = 0.4f
private const val ALPHA_SURFACE_VARIANT_LOW = 0.3f
private const val ALPHA_ON_SURFACE_MEDIUM = 0.7f
private const val BAR_WIDTH_DIVISOR = 2f
private const val MAX_VALUE_DEFAULT = 1f
private val PADDING_MEDIUM = 12.dp
private val PADDING_LARGE = 16.dp
private val PADDING_EXTRA_LARGE = 20.dp
private val CORNER_RADIUS_LARGE = 16.dp
private val CORNER_RADIUS_EXTRA_LARGE = 20.dp
