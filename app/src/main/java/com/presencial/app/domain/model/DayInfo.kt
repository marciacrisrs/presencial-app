package com.presencial.app.domain.model

import java.time.LocalDate

/**
 * Informações calculadas sobre um dia específico.
 */
data class DayInfo(
    val date: LocalDate,
    val status: DayStatus,
    val isWorkday: Boolean,
    val isHoliday: Boolean,
    val holidayName: String? = null,
    val isEditable: Boolean = false,
    val source: String = "MANUAL",
    val isPolicyRequired: Boolean = false
)
