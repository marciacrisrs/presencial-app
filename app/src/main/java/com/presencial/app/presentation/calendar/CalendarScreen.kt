package com.presencial.app.presentation.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.ui.components.MonthCalendarGrid
import com.presencial.app.ui.components.formatMonthYear
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    onNavigateToAbsences: () -> Unit = {}
) {
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val days by viewModel.calendarDays.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Calendário", style = MaterialTheme.typography.headlineLarge)
            TextButton(onClick = onNavigateToAbsences) {
                Text("Ausências")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = viewModel::previousMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Mês anterior")
            }
            Text(
                text = formatMonthYear(selectedMonth.year, selectedMonth.monthValue),
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = viewModel::nextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Próximo mês")
            }
        }

        MonthCalendarGrid(
            days = days,
            onDayClick = viewModel::selectDay,
            modifier = Modifier.fillMaxWidth()
        )
    }

    selectedDay?.let { day ->
        val dateLabel = "${day.date.dayOfMonth} de ${day.date.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"))}"
        AlertDialog(
            onDismissRequest = viewModel::dismissDayEditor,
            title = { Text("Editar — $dateLabel") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    day.holidayName?.let { Text("🎉 Feriado: $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary) }
                    if (day.source == "AUTOMATICO") {
                        Text("📍 Registrado automaticamente via GPS", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                    Text("Selecione o status do dia:")
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.updateDayStatus(DayStatus.PRESENCIAL) }) {
                    Text("🏢 Presencial")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.updateDayStatus(DayStatus.HOME_OFFICE) }) {
                        Text("🏠 Home Office")
                    }
                    TextButton(onClick = { viewModel.updateDayStatus(DayStatus.ABSENCE) }) {
                        Text("❌ Ausência")
                    }
                    TextButton(onClick = { viewModel.updateDayStatus(DayStatus.FUTURO) }) {
                        Text("🧹 Limpar")
                    }
                    TextButton(onClick = viewModel::dismissDayEditor) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }
}
