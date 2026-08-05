package com.presencial.app.domain.model

import java.time.LocalDate

enum class AbsenceType(val displayName: String) {
    VACATION("Férias"),
    DAY_OFF("Day off"),
    LICENSE("Licença"),
    ABSENCE("Ausência")
}

data class Absence(
    val id: Long = 0,
    val type: AbsenceType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isFullDay: Boolean,
    val hours: Float = 8f,
    val notes: String? = null,
    val isCounted: Boolean = false
)
