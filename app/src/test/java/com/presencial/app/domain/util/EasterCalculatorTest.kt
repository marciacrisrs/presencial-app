package com.presencial.app.domain.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class EasterCalculatorTest {

    @Test
    fun `pascoa 2024`() {
        assertEquals(LocalDate.of(2024, 3, 31), EasterCalculator.calculateEaster(2024))
    }

    @Test
    fun `pascoa 2025`() {
        assertEquals(LocalDate.of(2025, 4, 20), EasterCalculator.calculateEaster(2025))
    }

    @Test
    fun `pascoa 2026`() {
        assertEquals(LocalDate.of(2026, 4, 5), EasterCalculator.calculateEaster(2026))
    }

    @Test
    fun `pascoa 2027`() {
        assertEquals(LocalDate.of(2027, 3, 28), EasterCalculator.calculateEaster(2027))
    }
}
