package com.presencial.app.domain.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class TimeProviderTest {

    @Test
    fun `DefaultTimeProvider returns current dates and times`() {
        val provider = DefaultTimeProvider()
        
        val today = provider.today()
        val now = provider.now()
        val currentMonth = provider.currentMonth()
        
        val actualToday = LocalDate.now()
        val actualMonth = YearMonth.now()
        
        // We allow for a small difference if the test runs exactly at midnight
        assertTrue(today == actualToday || today == actualToday.minusDays(1))
        assertEquals(actualMonth, currentMonth)
        assertNotNull(now)
    }

    @Test
    fun `DefaultTimeProvider full cover`() {
        val provider = DefaultTimeProvider()
        assertNotNull(provider.now())
        assertNotNull(provider.today())
        assertNotNull(provider.currentMonth())
    }
}
