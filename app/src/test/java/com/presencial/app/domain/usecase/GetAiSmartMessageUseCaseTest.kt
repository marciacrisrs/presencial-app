package com.presencial.app.domain.usecase

import com.presencial.app.data.remote.AiIntelligenceService
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class GetAiSmartMessageUseCaseTest {

    private val aiService: AiIntelligenceService = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private lateinit var useCase: GetAiSmartMessageUseCase

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

    @BeforeEach
    fun setup() {
        every { settingsRepository.settings } returns flowOf(AppSettings())
        useCase = GetAiSmartMessageUseCase(aiService, settingsRepository)
    }

    @Test
    fun `when ai service returns message, then use ai message`() = runTest {
        every { settingsRepository.settings } returns flowOf(AppSettings(openAiApiKey = "sk-test"))
        coEvery { aiService.fetchSmartMessage(params, "sk-test") } returns "AI Hello"

        assertEquals("AI Hello", useCase(params))
    }

    @Test
    fun `when ai service fails, then use fallback`() = runTest {
        every { settingsRepository.settings } returns flowOf(AppSettings(openAiApiKey = "sk-test"))
        coEvery { aiService.fetchSmartMessage(params, "sk-test") } throws Exception("Network Error")

        assertEquals("Faltam 4 dias.", useCase(params))
    }

    @Test
    fun `when ai service returns null, then use fallback`() = runTest {
        every { settingsRepository.settings } returns flowOf(AppSettings(openAiApiKey = "sk-test"))
        coEvery { aiService.fetchSmartMessage(params, "sk-test") } returns null

        assertEquals("Faltam 4 dias.", useCase(params))
    }

    @Test
    fun `when no api key, then delegate to ai service local engine`() = runTest {
        coEvery { aiService.fetchSmartMessage(params, null) } returns "📅 Mensagem local"

        assertEquals("📅 Mensagem local", useCase(params))
    }
}
