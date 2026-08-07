package com.presencial.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presencial.app.domain.model.DashboardData
import com.presencial.app.domain.usecase.GetDashboardDataUseCase
import com.presencial.app.domain.usecase.ToggleTodayCheckInUseCase
import com.presencial.app.domain.util.TimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboardDataUseCase: GetDashboardDataUseCase,
    private val toggleTodayCheckInUseCase: ToggleTodayCheckInUseCase,
    private val timeProvider: TimeProvider
) : ViewModel() {

    val dashboardData: StateFlow<DashboardData?> = getDashboardDataUseCase(timeProvider.currentMonth())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    fun toggleTodayCheckIn(markPresencial: Boolean) {
        viewModelScope.launch {
            toggleTodayCheckInUseCase(markPresencial = markPresencial)
        }
    }

    fun markYesterdayPresencial() {
        viewModelScope.launch {
            toggleTodayCheckInUseCase(
                date = timeProvider.today().minusDays(1),
                markPresencial = true
            )
        }
    }
}

private const val STOP_TIMEOUT_MS = 5000L
