package com.presencial.app.domain.usecase

import java.time.LocalDate
import java.time.YearMonth

/**
 * Parâmetros para geração de mensagens inteligentes.
 */
data class SmartMessageParams(
    val completedDays: Int,
    val requiredDays: Int,
    val remainingDays: Int,
    val achievedPercentage: Float,
    val today: LocalDate,
    val yearMonth: YearMonth,
    val countSaturdays: Boolean
)
