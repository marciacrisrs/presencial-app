package com.presencial.app.data.preferences

import com.presencial.app.domain.model.PolicyConflictPriority
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.model.WeekParity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate

class PresencePolicyMapperTest {

    @Test
    fun `round trip should preserve policy fields`() {
        val policy = PresencePolicy(
            companyName = "Acme",
            freePercentageEnabled = true,
            freePercentage = 35,
            fixedWeekdaysEnabled = true,
            mandatoryWeekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            alternatingWeeksEnabled = true,
            alternatingAnchorDate = LocalDate.of(2026, 1, 5),
            onSiteWeekParity = WeekParity.ODD,
            conflictPriority = PolicyConflictPriority.FIXED_FIRST
        )

        val json = PresencePolicyMapper.toJson(policy)
        val restored = PresencePolicyMapper.fromJson(json, fallbackPercentage = 40)

        assertEquals("Acme", restored.companyName)
        assertEquals(35, restored.freePercentage)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), restored.mandatoryWeekdays)
        assertTrue(restored.alternatingWeeksEnabled)
        assertEquals(WeekParity.ODD, restored.onSiteWeekParity)
        assertEquals(PolicyConflictPriority.FIXED_FIRST, restored.conflictPriority)
    }
}
