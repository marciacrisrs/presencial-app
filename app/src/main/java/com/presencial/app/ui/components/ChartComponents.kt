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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.presencial.app.domain.model.AnnualSummary
import com.presencial.app.domain.model.MonthlySummary
import com.presencial.app.domain.model.WeeklyAttendanceSummary
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthlyBarChart(
    summaries: List<MonthlySummary>,
    modifier: Modifier = Modifier
) {
    if (summaries.isEmpty()) {
        Text("Sem dados para exibir", modifier = modifier.padding(PADDING_LARGE))
        return
    }

    ChartCard(title = "Comparecimento por mês", modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT_DP)) {
            val barWidth = size.width / (summaries.size * BAR_WIDTH_DIVISOR)
            val maxValue = summaries.maxOf { it.achievedPercentage }.coerceAtLeast(MAX_VALUE_DEFAULT)
            summaries.forEachIndexed { index, summary ->
                val barHeight = (summary.achievedPercentage / maxValue) * size.height * BAR_HEIGHT_FRACTION
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
        summaries.takeLast(MAX_VISIBLE_MONTHS).forEach { summary ->
            Text(
                text = formatMonthlySummaryLine(summary),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun WeeklyBarChart(
    summaries: List<WeeklyAttendanceSummary>,
    modifier: Modifier = Modifier
) {
    if (summaries.isEmpty()) {
        Text("Sem dados para exibir", modifier = modifier.padding(PADDING_LARGE))
        return
    }

    ChartCard(title = "Presença por semana", modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT_DP)) {
            val barWidth = size.width / (summaries.size * BAR_WIDTH_DIVISOR)
            val maxValue = summaries.maxOf { it.presencialDays }.coerceAtLeast(1).toFloat()
            summaries.forEachIndexed { index, summary ->
                val barHeight = (summary.presencialDays / maxValue) * size.height * BAR_HEIGHT_FRACTION
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
        summaries.forEach { summary ->
            Text(
                text = "${summary.label}: ${summary.presencialDays} dias",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun MonthlyTrendLineChart(
    summaries: List<MonthlySummary>,
    modifier: Modifier = Modifier
) {
    val sorted = summaries.sortedBy { it.yearMonth }
    if (sorted.isEmpty()) {
        Text("Sem dados para exibir", modifier = modifier.padding(PADDING_LARGE))
        return
    }

    ChartCard(title = "Evolução mensal", modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT_DP)) {
            if (sorted.size == 1) {
                val pointY = size.height * (1f - sorted.first().achievedPercentage / PERCENTAGE_MAX)
                drawCircle(
                    color = Color(COLOR_GREEN),
                    radius = LINE_POINT_RADIUS,
                    center = Offset(size.width / 2f, pointY)
                )
            } else {
                val maxValue = sorted.maxOf { it.achievedPercentage }.coerceAtLeast(MAX_VALUE_DEFAULT)
                val stepX = size.width / (sorted.size - 1)
                val path = Path()
                sorted.forEachIndexed { index, summary ->
                    val x = index * stepX
                    val y = size.height - (summary.achievedPercentage / maxValue) * size.height * BAR_HEIGHT_FRACTION
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    drawCircle(
                        color = Color(COLOR_GREEN),
                        radius = LINE_POINT_RADIUS,
                        center = Offset(x, y)
                    )
                }
                drawPath(
                    path = path,
                    color = Color(COLOR_GREEN),
                    style = Stroke(width = LINE_STROKE_WIDTH)
                )
            }
        }
        sorted.takeLast(MAX_VISIBLE_MONTHS).forEach { summary ->
            Text(
                text = formatMonthlySummaryLine(summary),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun AnnualSummaryCard(
    summary: AnnualSummary,
    modifier: Modifier = Modifier
) {
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
            Text("Resumo ${summary.year}", style = MaterialTheme.typography.titleLarge)
            Text(
                "Presença média: ${"%.1f".format(summary.averageAchieved)}%",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "Metas atingidas: ${summary.goalsMetCount} / ${summary.totalMonthsWithData} meses",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "Dias presenciais: ${summary.totalPresencial} (${summary.totalWorkdays} úteis)",
                style = MaterialTheme.typography.bodyMedium
            )
            summary.bestMonth?.let { best ->
                Text(
                    "Melhor mês: ${formatMonthLabel(best)} (${"%.0f".format(best.achievedPercentage)}%)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            summary.worstMonth?.let { worst ->
                Text(
                    "Pior mês: ${formatMonthLabel(worst)} (${"%.0f".format(worst.achievedPercentage)}%)",
                    style = MaterialTheme.typography.bodySmall
                )
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

@Composable
private fun ChartCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
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
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

private fun formatMonthlySummaryLine(summary: MonthlySummary): String {
    val month = summary.yearMonth.month.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR"))
    return "$month: ${"%.0f".format(summary.achievedPercentage)}% " +
        "(${summary.completedDays}/${summary.requiredDays})"
}

private fun formatMonthLabel(summary: MonthlySummary): String {
    val month = summary.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"))
    return "${month.replaceFirstChar { it.uppercase() }} ${summary.yearMonth.year}"
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
private const val PERCENTAGE_MAX = 100f
private const val LINE_STROKE_WIDTH = 4f
private const val LINE_POINT_RADIUS = 6f
private val PADDING_MEDIUM = 12.dp
private val PADDING_LARGE = 16.dp
private val PADDING_EXTRA_LARGE = 20.dp
private val CORNER_RADIUS_LARGE = 16.dp
private val CORNER_RADIUS_EXTRA_LARGE = 20.dp
