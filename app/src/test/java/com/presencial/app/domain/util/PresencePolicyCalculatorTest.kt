package com.presencial.app.domain.util

import com.presencial.app.domain.model.PolicyConflictPriority
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.model.WeekParity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class PresencePolicyCalculatorTest {

    private val yearMonth = YearMonth.of(2026, 8)

    @Test
    fun `percentage only should match goal calculator`() {
        val policy = PresencePolicy(
            freePercentageEnabled = true,
            freePercentage = 40,
            fixedWeekdaysEnabled = false,
            alternatingWeeksEnabled = false
        )
        val required = PresencePolicyCalculator.calculateRequiredDays(
            yearMonth,
            countSaturdays = false,
            absences = emptyList(),
            policy = policy
        )
        assertEquals(9, required)
    }

    @Test
    fun `fixed weekdays should count mandatory days in month`() {
        val policy = PresencePolicy(
            freePercentageEnabled = false,
            fixedWeekdaysEnabled = true,
            mandatoryWeekdays = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
            alternatingWeeksEnabled = false,
            conflictPriority = PolicyConflictPriority.UNION_MAX
        )
        val required = PresencePolicyCalculator.calculateRequiredDays(
            yearMonth,
            countSaturdays = false,
            absences = emptyList(),
            policy = policy
        )
        assertTrue(required >= 8)
    }

    @Test
    fun `union max should pick higher between percentage and fixed days`() {
        val policy = PresencePolicy(
            freePercentageEnabled = true,
            freePercentage = 20,
            fixedWeekdaysEnabled = true,
            mandatoryWeekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            alternatingWeeksEnabled = false,
            conflictPriority = PolicyConflictPriority.UNION_MAX
        )
        val required = PresencePolicyCalculator.calculateRequiredDays(
            yearMonth,
            countSaturdays = false,
            absences = emptyList(),
            policy = policy
        )
        assertTrue(required >= 5)
    }

    @Test
    fun `alternating weeks should mark on-site week workdays as required`() {
        val anchor = LocalDate.of(2026, 8, 3)
        val policy = PresencePolicy(
            freePercentageEnabled = false,
            fixedWeekdaysEnabled = false,
            alternatingWeeksEnabled = true,
            alternatingAnchorDate = anchor,
            onSiteWeekParity = WeekParity.EVEN
        )
        val onSiteDate = LocalDate.of(2026, 8, 5)
        val offSiteDate = LocalDate.of(2026, 8, 12)
        assertTrue(
            PresencePolicyCalculator.isPolicyRequired(onSiteDate, false, emptyList(), policy)
        )
        assertFalse(
            PresencePolicyCalculator.isPolicyRequired(offSiteDate, false, emptyList(), policy)
        )
    }

    @Test
    fun `validate should fail when no rule enabled`() {
        val result = PresencePolicyCalculator.validate(
            PresencePolicy(
                freePercentageEnabled = false,
                fixedWeekdaysEnabled = false,
                alternatingWeeksEnabled = false
            )
        )
        assertFalse(result.isValid)
    }

    @Test
    fun `weekly summaries should cover month weeks`() {
        val policy = PresencePolicy(
            fixedWeekdaysEnabled = true,
            mandatoryWeekdays = setOf(DayOfWeek.MONDAY)
        )
        val summaries = PresencePolicyCalculator.buildWeeklySummaries(
            yearMonth,
            countSaturdays = false,
            absences = emptyList(),
            policy = policy
        )
        assertTrue(summaries.size >= 4)
    }
}
