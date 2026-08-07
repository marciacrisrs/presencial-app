package com.presencial.app.domain.util

import com.presencial.app.domain.model.Absence
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * Calcula dias úteis considerando fins de semana, feriados e configuração de sábados.
 */
object WorkdayCalculator {

    fun isWeekend(date: LocalDate, countSaturdaysAsWorkdays: Boolean): Boolean {
        return when (date.dayOfWeek) {
            DayOfWeek.SUNDAY -> true
            DayOfWeek.SATURDAY -> !countSaturdaysAsWorkdays
            else -> false
        }
    }

    fun isWorkday(date: LocalDate, countSaturdaysAsWorkdays: Boolean): Boolean {
        if (HolidayCalculator.isHoliday(date)) return false
        return !isWeekend(date, countSaturdaysAsWorkdays)
    }

    fun countWorkdaysInMonth(
        yearMonth: YearMonth,
        countSaturdaysAsWorkdays: Boolean
    ): Int {
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()
        var count = 0
        var current = start
        while (!current.isAfter(end)) {
            if (isWorkday(current, countSaturdaysAsWorkdays)) count++
            current = current.plusDays(1)
        }
        return count
    }

    fun countLiquidWorkdaysInMonth(
        yearMonth: YearMonth,
        countSaturdaysAsWorkdays: Boolean,
        absences: List<Absence>
    ): Int {
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()
        val liquidWorkdays = mutableListOf<LocalDate>()
        var current = start
        while (!current.isAfter(end)) {
            if (isWorkday(current, countSaturdaysAsWorkdays)) {
                val isAbsent = absences.any { absence ->
                    val inRange = !current.isBefore(absence.startDate) && 
                                 !current.isAfter(absence.endDate)
                    !absence.isCounted && inRange && absence.isFullDay
                }
                if (!isAbsent) {
                    liquidWorkdays.add(current)
                }
            }
            current = current.plusDays(1)
        }
        return liquidWorkdays.size
    }

    fun countRemainingWorkdays(
        fromDate: LocalDate,
        yearMonth: YearMonth,
        countSaturdaysAsWorkdays: Boolean
    ): Int {
        val end = yearMonth.atEndOfMonth()
        if (fromDate.isAfter(end)) return 0
        var count = 0
        var current = fromDate
        while (!current.isAfter(end)) {
            if (isWorkday(current, countSaturdaysAsWorkdays)) count++
            current = current.plusDays(1)
        }
        return count
    }
}
