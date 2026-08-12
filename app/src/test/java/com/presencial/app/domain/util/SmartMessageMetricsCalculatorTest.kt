package com.presencial.app.domain.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SmartMessageMetricsCalculatorTest {

    @Test
    fun `calculateWeeklyRequiredDays should distribute remaining days`() {
        assertEquals(3, SmartMessageMetricsCalculator.calculateWeeklyRequiredDays(9, 15))
        assertEquals(0, SmartMessageMetricsCalculator.calculateWeeklyRequiredDays(0, 10))
    }

    @Test
    fun `calculateProjectedPercentage should estimate month end`() {
        val projected = SmartMessageMetricsCalculator.calculateProjectedPercentage(
            completedDays = 8,
            requiredDays = 12,
            remainingWorkdays = 10,
            remainingDays = 4
        )

        assertEquals(100, projected)
    }
}
