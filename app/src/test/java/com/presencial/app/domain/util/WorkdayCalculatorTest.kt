package com.presencial.app.domain.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class WorkdayCalculatorTest {

    @Test
    fun `janeiro 2026 conta dias uteis excluindo fim de semana e feriados`() {
        val yearMonth = YearMonth.of(2026, 1)
        // Jan 2026: 1 Jan holiday, weekends excluded
        val workdays = WorkdayCalculator.countWorkdaysInMonth(yearMonth, countSaturdaysAsWorkdays = false)
        assertTrue(workdays in 20..22)
    }

    @Test
    fun `sabado conta como dia util quando configurado`() {
        val saturday = LocalDate.of(2026, 1, 3) // Saturday, not holiday
        assertFalse(WorkdayCalculator.isWorkday(saturday, countSaturdaysAsWorkdays = false))
        assertTrue(WorkdayCalculator.isWorkday(saturday, countSaturdaysAsWorkdays = true))
    }

    @Test
    fun `domingo nunca e dia util`() {
        val sunday = LocalDate.of(2026, 1, 4)
        assertFalse(WorkdayCalculator.isWorkday(sunday, countSaturdaysAsWorkdays = false))
        assertFalse(WorkdayCalculator.isWorkday(sunday, countSaturdaysAsWorkdays = true))
    }

    @Test
    fun `feriado nao e dia util`() {
        val newYear = LocalDate.of(2026, 1, 1)
        assertFalse(WorkdayCalculator.isWorkday(newYear, countSaturdaysAsWorkdays = false))
    }

    @Test
    fun `countRemainingWorkdays from mid month`() {
        val yearMonth = YearMonth.of(2026, 8)
        val from = LocalDate.of(2026, 8, 15)
        val remaining = WorkdayCalculator.countRemainingWorkdays(from, yearMonth, false)
        assertTrue(remaining > 0)
        assertTrue(remaining <= 12)
    }

    @Test
    fun `countRemainingWorkdays return zero if fromDate is after month`() {
        val yearMonth = YearMonth.of(2026, 8)
        val from = LocalDate.of(2026, 9, 1)
        assertEquals(0, WorkdayCalculator.countRemainingWorkdays(from, yearMonth, false))
    }

    @Test
    fun `fevereiro ano bissexto conta dias corretamente`() {
        val leapYearMonth = YearMonth.of(2024, 2)
        val workdays = WorkdayCalculator.countWorkdaysInMonth(leapYearMonth, false)
        assertEquals(19, workdays) // Feb 2024: 21 workdays - 2 (Carnaval) = 19
    }

    @Test
    fun `countLiquidWorkdaysInMonth subtrai ausencias de dia inteiro que nao contam`() {
        val yearMonth = YearMonth.of(2026, 8)
        val absences = listOf(
            com.presencial.app.domain.model.Absence(
                id = 1L,
                type = com.presencial.app.domain.model.AbsenceType.VACATION,
                startDate = LocalDate.of(2026, 8, 3), // Mon
                endDate = LocalDate.of(2026, 8, 3),
                isFullDay = true,
                isCounted = false
            ),
            com.presencial.app.domain.model.Absence(
                id = 2L,
                type = com.presencial.app.domain.model.AbsenceType.VACATION,
                startDate = LocalDate.of(2026, 8, 10), // Mon
                endDate = LocalDate.of(2026, 8, 10),
                isFullDay = true,
                isCounted = true
            ),
            com.presencial.app.domain.model.Absence( // Overlapping with id 1
                id = 3L,
                type = com.presencial.app.domain.model.AbsenceType.LICENSE,
                startDate = LocalDate.of(2026, 8, 3),
                endDate = LocalDate.of(2026, 8, 4),
                isFullDay = true,
                isCounted = false
            )
        )
        val workdays = WorkdayCalculator.countWorkdaysInMonth(yearMonth, false)
        val liquid = WorkdayCalculator.countLiquidWorkdaysInMonth(yearMonth, false, absences)
        
        // Subtracts Aug 3 and Aug 4. (Aug 3 is shared by id 1 and 3, but only counted once).
        assertEquals(workdays - 2, liquid)
    }

    @Test
    fun `countLiquidWorkdaysInMonth nao subtrai ausencias parciais`() {
        val yearMonth = YearMonth.of(2026, 8)
        val absences = listOf(
            com.presencial.app.domain.model.Absence(
                id = 1L,
                type = com.presencial.app.domain.model.AbsenceType.ABSENCE,
                startDate = LocalDate.of(2026, 8, 3),
                endDate = LocalDate.of(2026, 8, 3),
                isFullDay = false,
                isCounted = false
            )
        )
        val workdays = WorkdayCalculator.countWorkdaysInMonth(yearMonth, false)
        val liquid = WorkdayCalculator.countLiquidWorkdaysInMonth(yearMonth, false, absences)
        assertEquals(workdays, liquid)
    }

    @Test
    fun `countRemainingWorkdays no ultimo dia do mes`() {
        val yearMonth = YearMonth.of(2026, 8)
        val from = LocalDate.of(2026, 8, 31) // Monday, workday
        val remaining = WorkdayCalculator.countRemainingWorkdays(from, yearMonth, false)
        assertEquals(1, remaining)
    }

    @Test
    fun `feriado que cai no sabado nao e dia util mesmo se contar sabados`() {
        // May 1st 2027 is a Saturday
        val holidaySat = LocalDate.of(2027, 5, 1)
        assertFalse(WorkdayCalculator.isWorkday(holidaySat, countSaturdaysAsWorkdays = true))
    }
}
