package com.presencial.app.domain.model

import java.time.YearMonth

/**
 * Resumo mensal de comparecimento presencial.
 */
data class MonthlySummary(
    val yearMonth: YearMonth,
    val workdays: Int,
    val requiredDays: Int,
    val completedDays: Int,
    val homeOfficeDays: Int,
    val requiredPercentage: Int,
    val achievedPercentage: Float
)
