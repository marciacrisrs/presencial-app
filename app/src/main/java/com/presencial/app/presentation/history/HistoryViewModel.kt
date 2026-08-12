package com.presencial.app.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presencial.app.domain.model.HistoryMonthData
import com.presencial.app.domain.model.WeeklyPolicySummary
import com.presencial.app.domain.usecase.GetHistoryUseCase
import com.presencial.app.domain.usecase.GetWeeklyPolicySummaryUseCase
import com.presencial.app.domain.util.TimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getHistoryUseCase: GetHistoryUseCase,
    getWeeklyPolicySummaryUseCase: GetWeeklyPolicySummaryUseCase,
    timeProvider: TimeProvider
) : ViewModel() {

    val historyMonths: StateFlow<List<HistoryMonthData>> = getHistoryUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val weeklySummaries: StateFlow<List<WeeklyPolicySummary>> =
        getWeeklyPolicySummaryUseCase(timeProvider.currentMonth())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())
}

private const val STOP_TIMEOUT_MS = 5000L
