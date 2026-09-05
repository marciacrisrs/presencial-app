package com.presencial.app.domain.util

import com.presencial.app.domain.model.Absence
import java.time.LocalDate

object AbsenceCoverage {
    fun coversFullDay(date: LocalDate, absences: List<Absence>): Boolean =
        absences.any { absence ->
            !date.isBefore(absence.startDate) &&
                !date.isAfter(absence.endDate) &&
                absence.isFullDay
        }
}
