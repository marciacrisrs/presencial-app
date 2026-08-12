package com.presencial.app.data.remote

import com.presencial.app.domain.usecase.SmartMessageParams
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class SmartMessagePromptBuilderTest {

    private val builder = SmartMessagePromptBuilder()

    private val params = SmartMessageParams(
        completedDays = 8,
        requiredDays = 12,
        remainingDays = 4,
        achievedPercentage = 66f,
        today = LocalDate.of(2026, 8, 12),
        yearMonth = YearMonth.of(2026, 8),
        countSaturdays = false,
        remainingWorkdays = 10,
        weeklyCompletedDays = 1,
        weeklyRequiredDays = 3,
        projectedMonthPercentage = 67
    )

    @Test
    fun `buildUserPrompt should include metrics`() {
        val prompt = builder.buildUserPrompt(params)

        assertTrue(prompt.contains("8 de 12"))
        assertTrue(prompt.contains("Faltam: 4"))
        assertTrue(prompt.contains("Dias úteis restantes no mês: 10"))
        assertTrue(prompt.contains("3 linhas numeradas"))
    }

    @Test
    fun `systemPrompt should describe assistant role`() {
        val prompt = builder.systemPrompt()

        assertTrue(prompt.contains("Presencial"))
        assertTrue(prompt.contains("presença presencial"))
    }
}
