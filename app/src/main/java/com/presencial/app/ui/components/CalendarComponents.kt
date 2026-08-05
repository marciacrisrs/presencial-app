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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            userScrollEnabled = false
        ) {
            items(weekDays) { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            val firstDayOfWeek = days.firstOrNull()?.date?.dayOfWeek?.value?.rem(7) ?: 0
            items(firstDayOfWeek) {
                Box(modifier = Modifier.aspectRatio(1f))
            }
            items(days, key = { it.date.toEpochDay() }) { dayInfo ->
                CalendarDayCell(dayInfo, dayInfo.date == today, onDayClick)
            }
        }
        CalendarLegend(modifier = Modifier.padding(top = 16.dp))
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
        DayStatus.FUTURO, DayStatus.FIM_DE_SEMANA -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        DayStatus.FERIADO -> Color(0xFF5D4037)
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                else Modifier
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
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (dayInfo.status == DayStatus.FUTURO || dayInfo.status == DayStatus.FIM_DE_SEMANA)
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            else if (dayInfo.status == DayStatus.HOME_OFFICE)
                MaterialTheme.colorScheme.onSurface
            else textColor
        )
    }
}

@Composable
fun CalendarLegend(modifier: Modifier = Modifier) {
    val items = listOf(
        "Presencial" to Color(0xFF1B873B),
        "Home Office" to Color(0xFF9AA0A6),
        "Faltou" to Color(0xFFD93025),
        "Hoje" to MaterialTheme.colorScheme.secondary,
        "Feriado" to Color(0xFFF9AB00),
        "Futuro" to Color.White
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false
    ) {
        items(items.size) { index ->
            val (label, color) = items[index]
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "● $label",
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }
        }
    }
}

fun dayColor(status: DayStatus, isToday: Boolean): Color {
    if (isToday && status != DayStatus.PRESENCIAL) return Color(0xFF1A73E8).copy(alpha = 0.3f)
    return when (status) {
        DayStatus.PRESENCIAL -> Color(0xFF1B873B)
        DayStatus.HOME_OFFICE -> Color(0xFF9AA0A6)
        DayStatus.FERIADO -> Color(0xFFF9AB00)
        DayStatus.FIM_DE_SEMANA -> Color.Transparent
        DayStatus.FUTURO -> Color.Transparent
        DayStatus.FALTOU -> Color(0xFFD93025).copy(alpha = 0.7f)
    }
}

fun formatMonthYear(year: Int, month: Int): String {
    val monthName = java.time.Month.of(month).getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
    return "${monthName.replaceFirstChar { it.uppercase() }} $year"
}
