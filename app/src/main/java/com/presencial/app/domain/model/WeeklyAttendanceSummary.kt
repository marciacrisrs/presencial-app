package com.presencial.app.domain.model

data class WeeklyAttendanceSummary(
    val weekIndex: Int,
    val label: String,
    val presencialDays: Int
)
