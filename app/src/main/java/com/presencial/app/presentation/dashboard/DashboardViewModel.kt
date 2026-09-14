package com.presencial.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presencial.app.domain.model.DashboardData
import com.presencial.app.domain.model.WorkAddress
import com.presencial.app.domain.repository.WorkAddressRepository
import com.presencial.app.domain.usecase.GetDashboardDataUseCase
import com.presencial.app.domain.usecase.ToggleTodayCheckInUseCase
import com.presencial.app.domain.util.TimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboardDataUseCase: GetDashboardDataUseCase,
    private val toggleTodayCheckInUseCase: ToggleTodayCheckInUseCase,
    private val timeProvider: TimeProvider,
    workAddressRepository: WorkAddressRepository
) : ViewModel() {

    // The dashboard month follows the calendar day so a long-lived process can refresh after midnight.
    private val dashboardDay = MutableStateFlow(timeProvider.today())

    @OptIn(ExperimentalCoroutinesApi::class)
    val dashboardData: StateFlow<DashboardData?> = dashboardDay
        .flatMapLatest { day -> getDashboardDataUseCase(YearMonth.from(day)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    val workAddresses: StateFlow<List<WorkAddress>> = workAddressRepository.getAllAddresses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val uiEventChannel = Channel<DashboardUiEvent>(Channel.BUFFERED)
    val uiEvents = uiEventChannel.receiveAsFlow()

    fun refreshIfDateChanged() {
        val today = timeProvider.today()
        if (dashboardDay.value != today) {
            dashboardDay.value = today
        }
    }

    fun toggleTodayCheckIn(markPresencial: Boolean) {
        viewModelScope.launch {
            toggleTodayCheckInUseCase(markPresencial = markPresencial)
            uiEventChannel.send(
                if (markPresencial) {
                    DashboardUiEvent.CheckInRegistered
                } else {
                    DashboardUiEvent.CheckInRemoved
                }
            )
        }
    }

    fun markYesterdayPresencial() {
        viewModelScope.launch {
            toggleTodayCheckInUseCase(
                date = timeProvider.today().minusDays(1),
                markPresencial = true
            )
            uiEventChannel.send(DashboardUiEvent.YesterdayCheckInRegistered)
        }
    }
}

private const val STOP_TIMEOUT_MS = 5000L
