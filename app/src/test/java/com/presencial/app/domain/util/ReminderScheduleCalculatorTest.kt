package com.presencial.app.domain.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.LocalTime

class ReminderScheduleCalculatorTest {

    @Test
    fun `schedules today when reminder time is still ahead`() {
        val now = LocalDateTime.of(2026, 8, 16, 17, 30)

        val delay = ReminderScheduleCalculator.initialDelayMillis(
            now = now,
            reminderTime = LocalTime.of(18, 0)
        )

        assertEquals(30 * 60 * 1000L, delay)
    }

    @Test
    fun `schedules next day when reminder time has already passed`() {
        val now = LocalDateTime.of(2026, 8, 16, 18, 1)

        val delay = ReminderScheduleCalculator.initialDelayMillis(
            now = now,
            reminderTime = LocalTime.of(18, 0)
        )

        assertEquals((23 * 60 + 59) * 60 * 1000L, delay)
    }

    @Test
    fun `schedules next day when reminder time is exactly now`() {
        val now = LocalDateTime.of(2026, 8, 16, 18, 0)

        val delay = ReminderScheduleCalculator.initialDelayMillis(
            now = now,
            reminderTime = LocalTime.of(18, 0)
        )

        assertEquals(24 * 60 * 60 * 1000L, delay)
    }
}
