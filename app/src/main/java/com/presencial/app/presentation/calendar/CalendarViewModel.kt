package com.presencial.app.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presencial.app.domain.model.DayInfo
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.usecase.GetMonthCalendarUseCase
import com.presencial.app.domain.usecase.UpdateDayStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getMonthCalendarUseCase: GetMonthCalendarUseCase,
    private val updateDayStatusUseCase: UpdateDayStatusUseCase
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth

    @OptIn(ExperimentalCoroutinesApi::class)
    val calendarDays: StateFlow<List<DayInfo>> = _selectedMonth
        .flatMapLatest { getMonthCalendarUseCase(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDay = MutableStateFlow<DayInfo?>(null)
    val selectedDay: StateFlow<DayInfo?> = _selectedDay

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun selectDay(day: DayInfo) {
        if (day.isEditable) _selectedDay.value = day
    }

    fun dismissDayEditor() {
        _selectedDay.value = null
    }

    fun updateDayStatus(status: DayStatus) {
        val day = _selectedDay.value ?: return
        viewModelScope.launch {
            updateDayStatusUseCase(day.date, status)
            _selectedDay.value = null
        }
    }
}
