package com.presencial.app.domain.model

import java.time.YearMonth

/**
 * Dados agregados exibidos no dashboard principal.
 */
data class DashboardData(
    val yearMonth: YearMonth,
    val totalDays: Int,
    val workdays: Int,
    val requiredDays: Int,
    val completedDays: Int,
    val remainingDays: Int,
    val homeOfficeDays: Int,
    val achievedPercentage: Float,
    val requiredPercentage: Int,
    val progressFraction: Float,
    val smartMessage: String,
    val isLoadingAi: Boolean = false,
    val countSaturdays: Boolean,
    val todayIsPresencial: Boolean,
    val todayIsWorkday: Boolean,
    val yesterdayIsPending: Boolean,
    val streak: Int
)
