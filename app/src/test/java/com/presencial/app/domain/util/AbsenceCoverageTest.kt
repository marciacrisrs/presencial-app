package com.presencial.app.domain.util

import com.presencial.app.domain.model.DayStatus
import com.presencial.app.util.TestDataFactory
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AbsenceCoverageTest {

    @Test
    fun `full-day absence covers dates inside the range inclusive`() {
        val absence = TestDataFactory.createAbsence(
            startDate = LocalDate.of(2026, 8, 10),
            endDate = LocalDate.of(2026, 8, 12),
            isFullDay = true
        )
        val absences = listOf(absence)

        assertTrue(AbsenceCoverage.coversFullDay(LocalDate.of(2026, 8, 10), absences))
        assertTrue(AbsenceCoverage.coversFullDay(LocalDate.of(2026, 8, 11), absences))
        assertTrue(AbsenceCoverage.coversFullDay(LocalDate.of(2026, 8, 12), absences))
        assertFalse(AbsenceCoverage.coversFullDay(LocalDate.of(2026, 8, 9), absences))
        assertFalse(AbsenceCoverage.coversFullDay(LocalDate.of(2026, 8, 13), absences))
    }

    @Test
    fun `partial-day absence does not cover the date for overlay totals`() {
        val absences = listOf(
            TestDataFactory.createAbsence(
                startDate = LocalDate.of(2026, 8, 10),
                endDate = LocalDate.of(2026, 8, 10),
                isFullDay = false,
                hours = 4f
            )
        )

        assertFalse(AbsenceCoverage.coversFullDay(LocalDate.of(2026, 8, 10), absences))
        assertTrue(
            AbsenceCoverage.isPresencialWorkday(
                TestDataFactory.createCheckIn(
                    date = LocalDate.of(2026, 8, 10),
                    status = DayStatus.PRESENCIAL
                ),
                absences
            )
        )
    }
}
