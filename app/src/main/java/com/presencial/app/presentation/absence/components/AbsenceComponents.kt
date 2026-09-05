package com.presencial.app.presentation.absence.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.presencial.app.R
import com.presencial.app.domain.model.Absence
import com.presencial.app.domain.model.AbsenceType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AbsenceItem(absence: Absence, onDelete: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val period = if (absence.startDate == absence.endDate) {
        absence.startDate.format(formatter)
    } else {
        "${absence.startDate.format(formatter)} - ${absence.endDate.format(formatter)}"
    }

    Card(
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = CARD_ALPHA)
        )
    ) {
        Row(
            modifier = Modifier.padding(SPACING_MEDIUM).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(SPACING_MEDIUM)
            ) {
                AbsenceIconBox(absence.type)
                AbsenceInfoColumn(absence, period)
            }
            AbsenceDeleteButton(onDelete)
        }
    }
}

@Composable
private fun AbsenceIconBox(type: AbsenceType) {
    Box(
        modifier = Modifier
            .size(ICON_BOX_SIZE)
            .clip(RoundedCornerShape(ICON_BOX_RADIUS))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = PRIMARY_CONTAINER_ALPHA)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (type) {
                AbsenceType.VACATION -> "🌴"
                AbsenceType.DAY_OFF -> "☕"
                AbsenceType.LICENSE -> "🏥"
                AbsenceType.ABSENCE -> "❌"
            },
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
private fun AbsenceInfoColumn(absence: Absence, period: String) {
    Column {
        Text(
            text = absence.type.displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = period, style = MaterialTheme.typography.bodyMedium)
        absence.notes?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = NOTES_ALPHA)
            )
        }
    }
}

@Composable
private fun AbsenceDeleteButton(onDelete: () -> Unit) {
    IconButton(onClick = onDelete) {
        Icon(
            Icons.Default.Delete,
            contentDescription = stringResource(R.string.cd_remove),
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun AddAbsenceDialog(
    onDismiss: () -> Unit,
    onConfirm: (AbsenceType, LocalDate, LocalDate, String?) -> Unit
) {
    var type by remember { mutableStateOf(AbsenceType.VACATION) }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    var notes by remember { mutableStateOf("") }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    AbsenceDatePickers(
        params = AbsenceDatePickerParams(
            showStartDatePicker = showStartDatePicker,
            showEndDatePicker = showEndDatePicker,
            startDate = startDate,
            endDate = endDate,
            onStartDateChange = { startDate = it },
            onEndDateChange = { endDate = it },
            onDismissStart = { showStartDatePicker = false },
            onDismissEnd = { showEndDatePicker = false }
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AbsenceDialogTitle() },
        text = {
            AbsenceDialogContent(
                params = AbsenceDialogContentParams(
                    type = type,
                    onTypeSelected = { type = it },
                    startDate = startDate,
                    endDate = endDate,
                    formatter = formatter,
                    onStartDateClick = { showStartDatePicker = true },
                    onEndDateClick = { showEndDatePicker = true },
                    notes = notes,
                    onNotesChange = { notes = it }
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(type, startDate, endDate, notes.takeIf { it.isNotBlank() }) },
                shape = RoundedCornerShape(DIALOG_CORNER_RADIUS)
            ) {
                Text("Salvar Registro")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

data class AbsenceDatePickerParams(
    val showStartDatePicker: Boolean,
    val showEndDatePicker: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val onStartDateChange: (LocalDate) -> Unit,
    val onEndDateChange: (LocalDate) -> Unit,
    val onDismissStart: () -> Unit,
    val onDismissEnd: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AbsenceDatePickers(params: AbsenceDatePickerParams) {
    val millisPerDay = HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE * MILLIS_PER_SECOND

    if (params.showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = params.startDate.toEpochDay() * millisPerDay
        )
        DatePickerDialog(
            onDismissRequest = params.onDismissStart,
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        params.onStartDateChange(LocalDate.ofEpochDay(it / millisPerDay))
                    }
                    params.onDismissStart()
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = params.onDismissStart) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (params.showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = params.endDate.toEpochDay() * millisPerDay
        )
        DatePickerDialog(
            onDismissRequest = params.onDismissEnd,
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        params.onEndDateChange(LocalDate.ofEpochDay(it / millisPerDay))
                    }
                    params.onDismissEnd()
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = params.onDismissEnd) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun AbsenceDialogTitle() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SPACING_SMALL)) {
        Icon(
            Icons.AutoMirrored.Filled.EventNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text("Registrar Ausência")
    }
}

data class AbsenceDialogContentParams(
    val type: AbsenceType,
    val onTypeSelected: (AbsenceType) -> Unit,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val formatter: DateTimeFormatter,
    val onStartDateClick: () -> Unit,
    val onEndDateClick: () -> Unit,
    val notes: String,
    val onNotesChange: (String) -> Unit
)

@Composable
private fun AbsenceDialogContent(params: AbsenceDialogContentParams) {
    Column(verticalArrangement = Arrangement.spacedBy(SPACING_MEDIUM)) {
        AbsenceTypeSelector(selectedType = params.type, onTypeSelected = params.onTypeSelected)

        AbsenceDateRangePicker(
            startDate = params.startDate,
            endDate = params.endDate,
            formatter = params.formatter,
            onStartDateClick = params.onStartDateClick,
            onEndDateClick = params.onEndDateClick
        )

        OutlinedTextField(
            value = params.notes,
            onValueChange = params.onNotesChange,
            label = { Text("Observações (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(DIALOG_CORNER_RADIUS),
            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AbsenceTypeSelector(
    selectedType: AbsenceType,
    onTypeSelected: (AbsenceType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(SPACING_SMALL)) {
        Text("Tipo de ausência", style = MaterialTheme.typography.labelLarge)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SPACING_SMALL),
            verticalArrangement = Arrangement.spacedBy(SPACING_TINY)
        ) {
            AbsenceType.entries.forEach { t ->
                FilterChip(
                    selected = selectedType == t,
                    onClick = { onTypeSelected(t) },
                    label = {
                        val emoji = when (t) {
                            AbsenceType.VACATION -> "🌴"
                            AbsenceType.DAY_OFF -> "☕"
                            AbsenceType.LICENSE -> "🏥"
                            AbsenceType.ABSENCE -> "❌"
                        }
                        Text("$emoji ${t.displayName}")
                    }
                )
            }
        }
    }
}

@Composable
private fun AbsenceDateRangePicker(
    startDate: LocalDate,
    endDate: LocalDate,
    formatter: DateTimeFormatter,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SPACING_SMALL)) {
        OutlinedCard(
            onClick = onStartDateClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(DIALOG_CORNER_RADIUS)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Início",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(startDate.format(formatter), style = MaterialTheme.typography.bodyMedium)
            }
        }
        OutlinedCard(
            onClick = onEndDateClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(DIALOG_CORNER_RADIUS)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Fim",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(endDate.format(formatter), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private const val CARD_ALPHA = 0.4f
private const val PRIMARY_CONTAINER_ALPHA = 0.5f
private const val NOTES_ALPHA = 0.6f
private val CARD_CORNER_RADIUS = 16.dp
private val ICON_BOX_SIZE = 48.dp
private val ICON_BOX_RADIUS = 12.dp
private val DIALOG_CORNER_RADIUS = 12.dp
private val SPACING_MEDIUM = 16.dp
private val SPACING_SMALL = 8.dp
private val SPACING_TINY = 4.dp
private const val HOURS_PER_DAY = 24
private const val MINUTES_PER_HOUR = 60
private const val SECONDS_PER_MINUTE = 60
private const val MILLIS_PER_SECOND = 1000L
