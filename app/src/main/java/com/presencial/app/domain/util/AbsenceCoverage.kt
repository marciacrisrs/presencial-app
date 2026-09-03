package com.presencial.app.domain.util

import com.presencial.app.domain.model.Absence
import com.presencial.app.domain.model.CheckIn
import com.presencial.app.domain.model.DayStatus
import java.time.LocalDate

/**
 * Full-day absences overlay check-ins on the calendar and in exports.
 * Totals that ignore this overlay treat vacation days as presencial/home office.
 */
object AbsenceCoverage {

    fun coversFullDay(date: LocalDate, absences: List<Absence>): Boolean =
        absences.any { absence ->
            absence.isFullDay &&
                !date.isBefore(absence.startDate) &&
                !date.isAfter(absence.endDate)
        }

    fun isPresencialWorkday(checkIn: CheckIn, absences: List<Absence>): Boolean =
        checkIn.status == DayStatus.PRESENCIAL && !coversFullDay(checkIn.date, absences)

    fun isHomeOfficeWorkday(checkIn: CheckIn, absences: List<Absence>): Boolean =
        checkIn.status == DayStatus.HOME_OFFICE && !coversFullDay(checkIn.date, absences)
}
