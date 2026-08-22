package com.presencial.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presencial.app.domain.model.WeeklyPolicySummary
import com.presencial.app.domain.usecase.GetWeeklyPolicySummaryUseCase
import com.presencial.app.domain.util.TimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WeeklyPolicySummaryViewModel @Inject constructor(
    getWeeklyPolicySummaryUseCase: GetWeeklyPolicySummaryUseCase,
    timeProvider: TimeProvider
) : ViewModel() {

    val summaries: StateFlow<List<WeeklyPolicySummary>> =
        getWeeklyPolicySummaryUseCase(timeProvider.currentMonth())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())
}

private const val STOP_TIMEOUT_MS = 5000L
