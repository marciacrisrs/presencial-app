package com.presencial.app.domain.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
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
}
