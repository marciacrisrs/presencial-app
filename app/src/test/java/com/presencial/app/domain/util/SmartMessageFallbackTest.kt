package com.presencial.app.domain.util

import com.presencial.app.domain.usecase.SmartMessageParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class SmartMessageFallbackTest {

    private val params = SmartMessageParams(
        completedDays = 8,
        requiredDays = 12,
        remainingDays = 4,
        achievedPercentage = 66f,
        today = LocalDate.of(2026, 8, 12),
        yearMonth = YearMonth.of(2026, 8),
        countSaturdays = false
    )

    @Test
    fun `should return faltam dias message`() {
        assertEquals("Faltam 4 dias.", SmartMessageFallback.generate(params))
    }

    @Test
    fun `should return singular dia`() {
        assertEquals(
            "Faltam 1 dia.",
            SmartMessageFallback.generate(params.copy(remainingDays = 1))
        )
    }

    @Test
    fun `should return meta concluida when completed`() {
        assertEquals(
            "Meta concluída 🎉",
            SmartMessageFallback.generate(params.copy(completedDays = 12, remainingDays = 0))
        )
    }
}
