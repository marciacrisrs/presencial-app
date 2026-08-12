package com.presencial.app.data.remote

import com.presencial.app.domain.usecase.SmartMessageParams
import com.presencial.app.domain.util.LocalSmartMessageEngine
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class AiIntelligenceServiceTest {

    private val openAiChatClient = mockk<OpenAiChatClient>()
    private val promptBuilder = mockk<SmartMessagePromptBuilder>()
    private val localSmartMessageEngine = mockk<LocalSmartMessageEngine>()
    private lateinit var service: AiIntelligenceService

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
    fun setUp() {
        service = AiIntelligenceService(openAiChatClient, promptBuilder, localSmartMessageEngine)
        every { promptBuilder.systemPrompt() } returns "system"
        every { promptBuilder.buildUserPrompt(any()) } returns "user"
    }

    @Test
    fun `given no api key, when fetchSmartMessage, then return local message`() = runTest {
        every { localSmartMessageEngine.generate(params) } returns "📅 Mensagem local"

        val result = service.fetchSmartMessage(params, apiKey = null)

        assertNotNull(result)
        assertEquals("📅 Mensagem local", result)
    }

    @Test
    fun `given api key and openai success, when fetchSmartMessage, then return selected variation`() = runTest {
        coEvery {
            openAiChatClient.chatCompletion("key", "system", "user")
        } returns Result.success(
            """
            1. 📅 Você pode fazer home office até sexta sem comprometer sua meta.
            2. ⚠️ Você precisará comparecer 3 vezes nesta semana.
            3. 🎯 Se mantiver o ritmo atual, terminará o mês com 67%.
            """.trimIndent()
        )

        val result = service.fetchSmartMessage(params, apiKey = "key")

        assertEquals(
            "📅 Você pode fazer home office até sexta sem comprometer sua meta.",
            result
        )
    }

    @Test
    fun `given api key and openai failure, when fetchSmartMessage, then return null`() = runTest {
        coEvery {
            openAiChatClient.chatCompletion("key", "system", "user")
        } returns Result.failure(IllegalStateException("network"))

        val result = service.fetchSmartMessage(params, apiKey = "key")

        assertNull(result)
    }
}
