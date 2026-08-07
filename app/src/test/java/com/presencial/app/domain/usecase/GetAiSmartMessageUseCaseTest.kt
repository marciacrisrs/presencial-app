package com.presencial.app.domain.usecase

import com.presencial.app.data.remote.AiIntelligenceService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class GetAiSmartMessageUseCaseTest {

    private val aiService: AiIntelligenceService = mockk()
    private lateinit var useCase: GetAiSmartMessageUseCase

    @BeforeEach
    fun setup() {
        useCase = GetAiSmartMessageUseCase(aiService)
    }

    @Test
    fun `given ai service returns message, when invoke, then return ai message`() = runTest {
        // Arrange
        val params = SmartMessageParams(
            completedDays = 5,
            requiredDays = 10,
            remainingDays = 5,
            achievedPercentage = 50f,
            today = LocalDate.of(2026, 8, 6),
            yearMonth = YearMonth.of(2026, 8),
            countSaturdays = false
        )
        coEvery { aiService.fetchSmartMessage(any(), any(), any(), any()) } returns "AI Hello"

        // Act
        val result = useCase(params)

        // Assert
        assertEquals("AI Hello", result)
    }

    @Test
    fun `given ai service fails, when invoke, then fallback to generator`() = runTest {
        // Arrange
        val params = SmartMessageParams(
            completedDays = 10,
            requiredDays = 10,
            remainingDays = 0,
            achievedPercentage = 100f,
            today = LocalDate.of(2026, 8, 6),
            yearMonth = YearMonth.of(2026, 8),
            countSaturdays = false
        )
        // Generator for 100% returns "Meta concluída 🎉"
        coEvery { aiService.fetchSmartMessage(any(), any(), any(), any()) } throws Exception("Network Error")

        // Act
        val result = useCase(params)

        // Assert
        assertEquals("Meta concluída 🎉", result)
    }

    @Test
    fun `given ai service returns null, when invoke, then fallback to generator`() = runTest {
        // Arrange
        val params = SmartMessageParams(
            completedDays = 0,
            requiredDays = 10,
            remainingDays = 10,
            achievedPercentage = 0f,
            today = LocalDate.of(2026, 8, 6),
            yearMonth = YearMonth.of(2026, 8),
            countSaturdays = false
        )
        coEvery { aiService.fetchSmartMessage(any(), any(), any(), any()) } returns null

        // Act
        val result = useCase(params)

        // Assert
        // For 0/10 in Aug 6 (Thu), remainingWorkdays = 21 - 4 = 17. 
        // Generator should return weekly distribution or similar.
        assertEquals("Você precisará ir 10 vezes nas próximas 3 semanas.", result)
    }
}
