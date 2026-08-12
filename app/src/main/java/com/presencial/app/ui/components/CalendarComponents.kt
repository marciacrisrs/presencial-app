package com.presencial.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.presencial.app.domain.model.DayInfo
import com.presencial.app.domain.model.DayStatus
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthCalendarGrid(
    days: List<DayInfo>,
    onDayClick: (DayInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    Column(modifier = modifier) {
        val weekDays = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
        
        // Header com dias da semana
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SPACING_GRID.dp)
        ) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).padding(SPACING_GRID.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = ALPHA_GRID_LABEL)
                )
            }
        }

        val firstDayOfWeek = days.firstOrNull()?.date?.dayOfWeek?.value?.rem(DAYS_IN_WEEK) ?: 0
        val gridItems = List(firstDayOfWeek) { null } + days
        val rows = gridItems.chunked(DAYS_IN_WEEK)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SPACING_GRID.dp)
        ) {
            rows.forEach { rowItems ->
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SPACING_GRID.dp)
                ) {
                    rowItems.forEach { dayInfo ->
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                            if (dayInfo != null) {
                                CalendarDayCell(dayInfo, dayInfo.date == today, onDayClick)
                            }
                        }
                    }
                    // Preencher o final da última linha se necessário
                    if (rowItems.size < DAYS_IN_WEEK) {
                        repeat(DAYS_IN_WEEK - rowItems.size) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
        }
        
        CalendarLegend(modifier = Modifier.padding(top = PADDING_LEGEND_TOP.dp))
    }
}

@Composable
private fun CalendarDayCell(
    dayInfo: DayInfo,
    isToday: Boolean,
    onDayClick: (DayInfo) -> Unit
) {
    val backgroundColor = dayColor(dayInfo.status, isToday)
    val textColor = when (dayInfo.status) {
        DayStatus.FUTURO, DayStatus.FIM_DE_SEMANA -> MaterialTheme.colorScheme.onSurface.copy(alpha = ALPHA_DAY_FUTURO)
        DayStatus.FERIADO -> Color(TEXT_COLOR_BROWN)
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (dayInfo.isPolicyRequired && dayInfo.isWorkday) {
                    Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier
                }
            )
            .then(
                if (isToday) {
                    Modifier.border(BORDER_WIDTH_TODAY.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), CircleShape)
                } else {
                    Modifier
                }
            )
            .then(
                if (dayInfo.isEditable) Modifier.clickable { onDayClick(dayInfo) }
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayInfo.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Normal,
            color = if (dayInfo.status == DayStatus.FUTURO || dayInfo.status == DayStatus.FIM_DE_SEMANA)
                MaterialTheme.colorScheme.onSurface.copy(alpha = ALPHA_DAY_LABEL)
            else if (dayInfo.status == DayStatus.HOME_OFFICE)
                MaterialTheme.colorScheme.onSurface
            else textColor
        )
    }
}

@Composable
fun CalendarLegend(modifier: Modifier = Modifier) {
    val items = listOf(
        "📌 Obrigatório" to MaterialTheme.colorScheme.primary,
        "🏢 Presencial" to Color(COLOR_GREEN),
        "🏠 Home Office" to Color(COLOR_GRAY),
        "❌ Faltou" to Color(COLOR_RED),
        "🧡 Ausência" to Color(COLOR_ORANGE),
        "🔵 Hoje" to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
        "🎉 Feriado" to Color(COLOR_YELLOW)
    )
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SPACING_LEGEND.dp)
    ) {
        items.chunked(LEGEND_COLUMNS).forEach { rowItems ->
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SPACING_LEGEND.dp)
            ) {
                rowItems.forEach { (label, color) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(CORNER_RADIUS_LEGEND.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = ALPHA_LEGEND_BG))
                            .padding(horizontal = PADDING_LEGEND_ITEM_H.dp, vertical = PADDING_LEGEND_ITEM_V.dp)
                    ) {
                        Text(
                            text = "● $label",
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            maxLines = 1
                        )
                    }
                }
                // Preencher o final da linha se necessário
                if (rowItems.size < LEGEND_COLUMNS) {
                    repeat(LEGEND_COLUMNS - rowItems.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

fun dayColor(status: DayStatus, isToday: Boolean): Color {
    val isMarked = status == DayStatus.PRESENCIAL || status == DayStatus.ABSENCE
    if (isToday && !isMarked) {
        return Color(COLOR_BLUE_TODAY).copy(alpha = 0.12f)
    }
    return when (status) {
        DayStatus.PRESENCIAL -> Color(COLOR_GREEN)
        DayStatus.HOME_OFFICE -> Color(COLOR_GRAY)
        DayStatus.FERIADO -> Color(COLOR_YELLOW)
        DayStatus.FIM_DE_SEMANA -> Color.Transparent
        DayStatus.FUTURO -> Color.Transparent
        DayStatus.FALTOU -> Color(COLOR_RED).copy(alpha = ALPHA_FALTOU)
        DayStatus.ABSENCE -> Color(COLOR_ORANGE)
    }
}

fun formatMonthYear(year: Int, month: Int): String {
    val monthName = java.time.Month.of(month).getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"))
    return "${monthName.replaceFirstChar { it.uppercase() }} $year"
}

private const val DAYS_IN_WEEK = 7
private const val LEGEND_COLUMNS = 3
private const val COLOR_GREEN = 0xFF1B873B
private const val COLOR_GRAY = 0xFF9AA0A6
private const val COLOR_RED = 0xFFD93025
private const val COLOR_ORANGE = 0xFFFF8C00
private const val COLOR_YELLOW = 0xFFF9AB00
private const val COLOR_BLUE_TODAY = 0xFF1A73E8
private const val TEXT_COLOR_BROWN = 0xFF5D4037

private const val SPACING_GRID = 4
private const val SPACING_LEGEND = 8
private const val PADDING_LEGEND_TOP = 16
private const val PADDING_LEGEND_ITEM_H = 8
private const val PADDING_LEGEND_ITEM_V = 4
private const val ALPHA_GRID_LABEL = 0.6f
private const val ALPHA_DAY_FUTURO = 0.5f
private const val ALPHA_DAY_LABEL = 0.6f
private const val ALPHA_LEGEND_BG = 0.3f
private const val ALPHA_FALTOU = 0.7f
private const val BORDER_WIDTH_TODAY = 1
private const val CORNER_RADIUS_LEGEND = 8
