package com.presencial.app.domain.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class HolidayCalculatorTest {

    @Test
    fun `feriados fixos de 2026`() {
        assertNotNull(HolidayCalculator.getHoliday(LocalDate.of(2026, 1, 1)))
        assertNotNull(HolidayCalculator.getHoliday(LocalDate.of(2026, 4, 21)))
        assertNotNull(HolidayCalculator.getHoliday(LocalDate.of(2026, 12, 25)))
    }

    @Test
    fun `consciencia negra inclusa`() {
        val holiday = HolidayCalculator.getHoliday(LocalDate.of(2026, 11, 20))
        assertNotNull(holiday)
        assertEquals("Consciência Negra", holiday?.name)
    }

    @Test
    fun `feriados moveis baseados na pascoa 2026`() {
        val easter = EasterCalculator.calculateEaster(2026)
        assertEquals(LocalDate.of(2026, 4, 5), easter)

        assertNotNull(HolidayCalculator.getHoliday(easter.minusDays(2))) // Sexta-feira Santa
        assertNotNull(HolidayCalculator.getHoliday(easter.plusDays(60))) // Corpus Christi
    }

    @Test
    fun `total de feriados nacionais por ano`() {
        val holidays = HolidayCalculator.getHolidaysForYear(2026)
        // 9 fixos + 4 moveis (2 carnaval + sexta + corpus)
        assertTrue(holidays.size >= 13)
    }

    @Test
    fun `dia comum nao e feriado`() {
        assertEquals(null, HolidayCalculator.getHoliday(LocalDate.of(2026, 3, 10)))
    }
}
