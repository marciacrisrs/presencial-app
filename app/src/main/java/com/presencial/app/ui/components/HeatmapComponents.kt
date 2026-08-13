package com.presencial.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.presencial.app.R
import com.presencial.app.domain.model.DayInfo
import com.presencial.app.domain.model.DayStatus
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun YearHeatmapCard(
    year: Int,
    days: List<DayInfo>,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onDayClick: (DayInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CORNER_RADIUS),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = ALPHA_SURFACE)
        )
    ) {
        Column(
            modifier = Modifier.padding(PADDING),
            verticalArrangement = Arrangement.spacedBy(PADDING)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousYear,
                    modifier = Modifier.semantics {
                        contentDescription = "Ano anterior"
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                }
                Text(
                    text = stringResource(R.string.statistics_heatmap_title, year),
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(
                    onClick = onNextYear,
                    modifier = Modifier.semantics {
                        contentDescription = "Próximo ano"
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }

            if (days.isEmpty()) {
                Text(stringResource(R.string.statistics_no_data))
            } else {
                YearHeatmapGrid(days = days, onDayClick = onDayClick)
                HeatmapLegend()
            }
        }
    }
}

@Composable
private fun YearHeatmapGrid(
    days: List<DayInfo>,
    onDayClick: (DayInfo) -> Unit
) {
    val today = remember { java.time.LocalDate.now() }
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
    val weeks = remember(days) { buildHeatmapWeeks(days) }

    Column(verticalArrangement = Arrangement.spacedBy(CELL_SPACING)) {
        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CELL_SPACING)
            ) {
                week.forEach { dayInfo ->
                    if (dayInfo == null) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val color = heatmapColor(dayInfo.status, dayInfo.date == today)
                        val description = "${formatter.format(dayInfo.date)}: ${dayInfo.status.name}"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(CELL_RADIUS))
                                .background(color)
                                .border(
                                    width = if (dayInfo.date == today) TODAY_BORDER else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(CELL_RADIUS)
                                )
                                .clickable { onDayClick(dayInfo) }
                                .semantics { contentDescription = description }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeatmapLegendItem(color = heatmapColor(DayStatus.PRESENCIAL, false), label = "Presencial")
        HeatmapLegendItem(color = heatmapColor(DayStatus.HOME_OFFICE, false), label = "Home Office")
        HeatmapLegendItem(color = heatmapColor(DayStatus.FERIADO, false), label = "Feriado")
    }
}

@Composable
private fun HeatmapLegendItem(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

private fun buildHeatmapWeeks(days: List<DayInfo>): List<List<DayInfo?>> {
    if (days.isEmpty()) return emptyList()
    val sorted = days.sortedBy { it.date }
    val firstDayOffset = sorted.first().date.dayOfWeek.value.rem(DAYS_IN_WEEK)
    val gridItems = List(firstDayOffset) { null as DayInfo? } + sorted
    return gridItems.chunked(DAYS_IN_WEEK).map { week ->
        if (week.size < DAYS_IN_WEEK) week + List(DAYS_IN_WEEK - week.size) { null } else week
    }
}

private fun heatmapColor(status: DayStatus, isToday: Boolean): Color {
    val base = dayColor(status, isToday)
    return when (status) {
        DayStatus.FIM_DE_SEMANA, DayStatus.FUTURO -> Color(COLOR_HEATMAP_EMPTY)
        else -> base.copy(alpha = HEATMAP_ALPHA)
    }
}

private const val COLOR_HEATMAP_EMPTY = 0xFFE8EAED

private const val DAYS_IN_WEEK = 7
private const val ALPHA_SURFACE = 0.4f
private const val HEATMAP_ALPHA = 0.85f
private val PADDING = 16.dp
private val CORNER_RADIUS = 20.dp
private val CELL_SPACING = 2.dp
private val CELL_RADIUS = 3.dp
private val TODAY_BORDER = 1.dp
