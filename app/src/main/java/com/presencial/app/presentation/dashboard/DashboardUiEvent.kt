package com.presencial.app.presentation.dashboard

sealed interface DashboardUiEvent {
    data object CheckInRegistered : DashboardUiEvent
    data object CheckInRemoved : DashboardUiEvent
    data object YesterdayCheckInRegistered : DashboardUiEvent
}
