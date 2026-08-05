package com.presencial.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
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
}
