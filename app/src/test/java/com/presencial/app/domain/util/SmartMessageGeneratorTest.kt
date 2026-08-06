package com.presencial.app.domain.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDate
import java.time.YearMonth

class SmartMessageGeneratorTest {

    @Test
    fun `given requiredDays is zero, when generate, then return configuration message`() {
        val message = SmartMessageGenerator.generate(
            0, 0, 0, 0f,
            LocalDate.now(), YearMonth.now(), false
        )
        assertEquals("Configure seu percentual de presença nas configurações.", message)
    }

    @Test
    fun `given goal achieved, when generate, then return success message`() {
        val message = SmartMessageGenerator.generate(
            10, 10, 0, 100f,
            LocalDate.now(), YearMonth.now(), false
        )
        assertEquals("Meta concluída 🎉", message)
    }

    @ParameterizedTest
    @CsvSource(
        "8, 10, 2, 80, 2026-08-06, Você já cumpriu 80% da meta.",
        "2, 10, 8, 20, 2026-08-06, Você precisará ir 8 vezes nas próximas 3 semanas.",
        "1, 10, 9, 10, 2026-08-28, Atenção: faltam 9 dias e restam apenas 1 dias úteis."
    )
    fun `scenarios for smart messages`(
        completed: Int,
        required: Int,
        remaining: Int,
        percentage: Float,
        dateStr: String,
        expected: String
    ) {
        val today = LocalDate.parse(dateStr)
        val yearMonth = YearMonth.from(today)
        val message = SmartMessageGenerator.generate(
            completed, required, remaining, percentage,
            today, yearMonth, false
        )
        assertEquals(expected, message)
    }
}
