package com.presencial.app.domain.util

import com.presencial.app.domain.usecase.SmartMessageParams
import com.presencial.app.util.FakeSmartMessageTextProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class LocalSmartMessageEngineTest {

    private val engine = LocalSmartMessageEngine(FakeSmartMessageTextProvider())

    private fun params(
        completedDays: Int = 8,
        requiredDays: Int = 12,
        remainingDays: Int = 4,
        achievedPercentage: Float = 66f,
        remainingWorkdays: Int = 10,
        weeklyCompletedDays: Int = 1,
        weeklyRequiredDays: Int = 3,
        projectedMonthPercentage: Int = 67
    ) = SmartMessageParams(
        completedDays = completedDays,
        requiredDays = requiredDays,
        remainingDays = remainingDays,
        achievedPercentage = achievedPercentage,
        today = LocalDate.of(2026, 8, 12),
        yearMonth = YearMonth.of(2026, 8),
        countSaturdays = false,
        remainingWorkdays = remainingWorkdays,
        weeklyCompletedDays = weeklyCompletedDays,
        weeklyRequiredDays = weeklyRequiredDays,
        projectedMonthPercentage = projectedMonthPercentage
    )

    @Test
    fun `should return configure message when no required days`() {
        val result = engine.generate(params(requiredDays = 0, remainingDays = 0))
        assertEquals("Configure seu percentual de presença nas configurações.", result)
    }

    @Test
    fun `should return goal met when completed`() {
        val result = engine.generate(params(completedDays = 12, remainingDays = 0))
        assertTrue(result.contains("Meta batida"))
    }

    @Test
    fun `should return weekly required when behind this week`() {
        val result = engine.generate(
            params(
                remainingDays = 4,
                remainingWorkdays = 10,
                weeklyCompletedDays = 0,
                weeklyRequiredDays = 3,
                achievedPercentage = 66f,
                projectedMonthPercentage = 0
            )
        )
        assertTrue(result.contains("3 vezes nesta semana"))
    }

    @Test
    fun `should return remaining days as default`() {
        val result = engine.generate(
            params(
                remainingDays = 5,
                remainingWorkdays = 10,
                weeklyRequiredDays = 0,
                achievedPercentage = 66f,
                projectedMonthPercentage = 0
            )
        )
        assertEquals("📅 Faltam 5 dias para a meta.", result)
    }
}
