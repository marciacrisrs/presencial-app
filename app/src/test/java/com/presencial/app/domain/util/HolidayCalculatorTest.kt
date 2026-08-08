package com.presencial.app.domain.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class HolidayCalculatorTest {

    @Test
    fun `todos feriados fixos estao presentes`() {
        val year = 2026
        val holidays = HolidayCalculator.getHolidaysForYear(year)
        val names = holidays.map { it.name }
        
        assertTrue(names.contains("Confraternização Universal"))
        assertTrue(names.contains("Tiradentes"))
        assertTrue(names.contains("Dia do Trabalho"))
        assertTrue(names.contains("Independência"))
        assertTrue(names.contains("Nossa Senhora Aparecida"))
        assertTrue(names.contains("Finados"))
        assertTrue(names.contains("Proclamação da República"))
        assertTrue(names.contains("Consciência Negra"))
        assertTrue(names.contains("Natal"))
    }

    @Test
    fun `todos feriados moveis estao presentes`() {
        val year = 2026
        val holidays = HolidayCalculator.getHolidaysForYear(year)
        val names = holidays.map { it.name }
        
        assertEquals(2, names.count { it == "Carnaval" })
        assertTrue(names.contains("Sexta-feira Santa"))
        assertTrue(names.contains("Corpus Christi"))
    }

    @Test
    fun `getHoliday identifica feriados corretamente`() {
        assertTrue(HolidayCalculator.isHoliday(LocalDate.of(2026, 1, 1)))
        assertTrue(HolidayCalculator.isHoliday(LocalDate.of(2026, 12, 25)))
        assertFalse(HolidayCalculator.isHoliday(LocalDate.of(2026, 1, 2)))
        
        val holiday = HolidayCalculator.getHoliday(LocalDate.of(2026, 11, 20))
        assertEquals("Consciência Negra", holiday?.name)
    }

    @Test
    fun `feriados moveis baseados na pascoa 2026`() {
        val easter = EasterCalculator.calculateEaster(2026)
        assertEquals(LocalDate.of(2026, 4, 5), easter)

        assertNotNull(HolidayCalculator.getHoliday(easter.minusDays(48))) // Carnaval Seg
        assertNotNull(HolidayCalculator.getHoliday(easter.minusDays(47))) // Carnaval Ter
        assertNotNull(HolidayCalculator.getHoliday(easter.minusDays(2))) // Sexta-feira Santa
        assertNotNull(HolidayCalculator.getHoliday(easter.plusDays(60))) // Corpus Christi
    }
}
