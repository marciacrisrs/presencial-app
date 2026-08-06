package com.presencial.app.domain.model

import java.time.LocalDate

/**
 * Registro de check-in diário do usuário.
 */
data class CheckIn(
    val date: LocalDate,
    val status: DayStatus,
    val updatedAt: Long = System.currentTimeMillis(),
    val source: String = "MANUAL"
)
