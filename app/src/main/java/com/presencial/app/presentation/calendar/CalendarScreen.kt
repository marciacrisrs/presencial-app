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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
        DayEditorDialog(
            day = day,
            onStatusSelected = viewModel::updateDayStatus,
            onDismiss = viewModel::dismissDayEditor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayEditorDialog(
    day: com.presencial.app.domain.model.DayInfo,
    onStatusSelected: (DayStatus) -> Unit,
    onDismiss: () -> Unit
) {
    val monthName = day.date.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"))
    val dateLabel = "${day.date.dayOfMonth} de $monthName"

    var selectedStatus by remember {
        mutableStateOf(
            when (day.status) {
                DayStatus.PRESENCIAL, DayStatus.HOME_OFFICE, DayStatus.ABSENCE -> day.status
                else -> DayStatus.HOME_OFFICE
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar — $dateLabel") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DayInfoDetails(day)
                Text("Selecione o status do dia:")
                StatusSelector(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { selectedStatus = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onStatusSelected(selectedStatus) }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            DialogDismissButtons(
                onClear = { onStatusSelected(DayStatus.FUTURO) },
                onDismiss = onDismiss
            )
        }
    )
}

@Composable
private fun DayInfoDetails(day: com.presencial.app.domain.model.DayInfo) {
    day.holidayName?.let {
        Text(
            "🎉 Feriado: $it",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
    if (day.source == "AUTOMATICO") {
        Text(
            "📍 Registrado automaticamente via GPS",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusSelector(
    selectedStatus: DayStatus,
    onStatusSelected: (DayStatus) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val options = listOf(
            DayStatus.PRESENCIAL to "🏢 Presencial",
            DayStatus.HOME_OFFICE to "🏠 Home Office",
            DayStatus.ABSENCE to "❌ Ausência"
        )

        options.forEach { (status, label) ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun DialogDismissButtons(
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    Row {
        TextButton(onClick = onClear) {
            Text("Limpar")
        }
        TextButton(onClick = onDismiss) {
            Text("Cancelar")
        }
    }
}
