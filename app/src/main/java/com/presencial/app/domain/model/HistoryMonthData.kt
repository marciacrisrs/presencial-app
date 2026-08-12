package com.presencial.app.domain.model

import java.time.YearMonth

data class HistoryMonthData(
    val summary: MonthlySummary,
    val autoCheckInDays: Int
)
