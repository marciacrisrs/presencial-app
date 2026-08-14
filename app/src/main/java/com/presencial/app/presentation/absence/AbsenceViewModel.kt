package com.presencial.app.presentation.absence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presencial.app.domain.model.Absence
import com.presencial.app.domain.model.AbsenceType
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.widget.WidgetRefresher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AbsenceViewModel @Inject constructor(
    private val absenceRepository: AbsenceRepository,
    private val widgetRefresher: WidgetRefresher
) : ViewModel() {

    val absences: StateFlow<List<Absence>> = absenceRepository.getAllAbsences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun addAbsence(
        type: AbsenceType,
        startDate: LocalDate,
        endDate: LocalDate,
        isFullDay: Boolean = true,
        hours: Float = 8f,
        notes: String? = null
    ) {
        if (endDate.isBefore(startDate)) {
            _message.value = "A data final não pode ser anterior à data inicial"
            return
        }

        viewModelScope.launch {
            absenceRepository.insertAbsence(
                Absence(
                    type = type,
                    startDate = startDate,
                    endDate = endDate,
                    isFullDay = isFullDay,
                    hours = hours,
                    notes = notes,
                    isCounted = false // Default to not counted for these types
                )
            )
            widgetRefresher.refresh()
            _message.value = "Registro adicionado com sucesso"
        }
    }

    fun deleteAbsence(id: Long) {
        viewModelScope.launch {
            absenceRepository.deleteById(id)
            widgetRefresher.refresh()
            _message.value = "Registro removido"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

private const val STOP_TIMEOUT_MS = 5000L
