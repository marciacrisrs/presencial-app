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
import androidx.compose.ui.unit.dp
import com.presencial.app.domain.model.MonthlySummary
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthlyBarChart(
    summaries: List<MonthlySummary>,
    modifier: Modifier = Modifier
) {
    if (summaries.isEmpty()) {
        Text("Sem dados para exibir", modifier = modifier.padding(16.dp))
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Comparecimento por mês", style = MaterialTheme.typography.titleLarge)
            Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                val barWidth = size.width / (summaries.size * 2f)
                val maxValue = summaries.maxOf { it.achievedPercentage }.coerceAtLeast(1f)
                summaries.forEachIndexed { index, summary ->
                    val barHeight = (summary.achievedPercentage / maxValue) * size.height * 0.85f
                    val x = index * (barWidth * 2) + barWidth * 0.5f
                    val y = size.height - barHeight
                    drawRoundRect(
                        color = Color(0xFF1B873B),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
            }
            summaries.takeLast(6).forEach { summary ->
                val month = summary.yearMonth.month.getDisplayName(TextStyle.SHORT, Locale("pt", "BR"))
                Text(
                    text = "$month: ${"%.0f".format(summary.achievedPercentage)}% (${summary.completedDays}/${summary.requiredDays})",
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text(value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}
