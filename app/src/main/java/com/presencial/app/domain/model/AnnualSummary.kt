package com.presencial.app.domain.model

data class AnnualSummary(
    val year: Int,
    val averageAchieved: Float,
    val totalWorkdays: Int,
    val totalPresencial: Int,
    val goalsMetCount: Int,
    val totalMonthsWithData: Int,
    val bestMonth: MonthlySummary?,
    val worstMonth: MonthlySummary?
)
