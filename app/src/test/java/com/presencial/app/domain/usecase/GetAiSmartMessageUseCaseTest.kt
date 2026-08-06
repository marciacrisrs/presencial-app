package com.presencial.app.domain.usecase

import com.presencial.app.data.remote.AiIntelligenceService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import java.time.YearMonth
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
        coEvery { aiService.fetchSmartMessage(any(), any(), any(), any()) } returns "AI Message"

        // Act
        val result = useCase(
            5, 10, 5, 50f,
            LocalDate.now(), YearMonth.now(), false
        )

        // Assert
        assertEquals("AI Message", result)
    }

    @Test
    fun `given ai service fails, when invoke, then return fallback message`() = runTest {
        // Arrange
        coEvery { aiService.fetchSmartMessage(any(), any(), any(), any()) } throws Exception("Error")

        // Act
        val result = useCase(
            5, 10, 5, 50f,
            LocalDate.now(), YearMonth.now(), false
        )

        // Assert
        assertTrue(result.isNotEmpty())
        assertNotEquals("AI Message", result)
    }
}
