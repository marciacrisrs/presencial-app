package com.presencial.app.domain.util

import com.presencial.app.util.TestDataFactory
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AbsenceCoverageTest {

    private val today = LocalDate.of(2026, 9, 5)

    @Test
    fun `full-day absence covering the date returns true`() {
        val absences = listOf(
            TestDataFactory.createAbsence(
                startDate = today.minusDays(1),
                endDate = today.plusDays(1),
                isFullDay = true
            )
        )

        assertTrue(AbsenceCoverage.coversFullDay(today, absences))
    }

    @Test
    fun `partial-day absence covering the date returns false`() {
        val absences = listOf(
            TestDataFactory.createAbsence(
                startDate = today,
                endDate = today,
                isFullDay = false,
                hours = 4f
            )
        )

        assertFalse(AbsenceCoverage.coversFullDay(today, absences))
    }

    @Test
    fun `absence outside the date returns false`() {
        val absences = listOf(
            TestDataFactory.createAbsence(
                startDate = today.minusDays(5),
                endDate = today.minusDays(2),
                isFullDay = true
            )
        )

        assertFalse(AbsenceCoverage.coversFullDay(today, absences))
    }
}
