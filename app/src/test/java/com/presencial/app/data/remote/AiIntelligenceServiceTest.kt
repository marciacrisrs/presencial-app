package com.presencial.app.data.remote

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.random.Random

class AiIntelligenceServiceTest {

    private lateinit var service: AiIntelligenceService

    @BeforeEach
    fun setUp() {
        service = AiIntelligenceService()
        mockkObject(Random.Default)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(Random.Default)
    }

    @Test
    fun `given fail chance hits, when fetchSmartMessage, then return null`() = runTest {
        // Arrange
        every { Random.nextInt(100) } returns 4 // Fail chance is 5

        // Act
        val result = service.fetchSmartMessage(10, 20, 10, 50)

        // Assert
        assertNull(result)
    }

    @Test
    fun `given goal completed, when fetchSmartMessage, then return success message`() = runTest {
        // Arrange
        every { Random.nextInt(100) } returns 10 // No fail

        // Act
        val result = service.fetchSmartMessage(20, 20, 10, 100)

        // Assert
        assertEquals("🎉 Meta batida! Aproveite o home office sem culpa.", result)
    }

    @Test
    fun `given more days remaining than workdays, when fetchSmartMessage, then return alert message`() = runTest {
        // Arrange
        every { Random.nextInt(100) } returns 10

        // Act
        val result = service.fetchSmartMessage(5, 20, 10, 25)

        // Assert
        assertEquals("⚠️ Alerta: Você precisa ir todos os dias restantes para atingir a meta.", result)
    }

    @Test
    fun `given many workdays compared to remaining, when fetchSmartMessage, then return home office message`() = runTest {
        // Arrange
        every { Random.nextInt(100) } returns 10

        // Act
        val result = service.fetchSmartMessage(18, 20, 10, 90)
        // remaining = 2. remainingWorkdays = 10. 10 > 2 * 2 (4).

        // Assert
        assertEquals("📅 Você pode fazer home office até sexta sem comprometer sua meta.", result)
    }

    @Test
    fun `given few remaining days, when fetchSmartMessage, then return almost there message`() = runTest {
        // Arrange
        every { Random.nextInt(100) } returns 10

        // Act
        val result = service.fetchSmartMessage(18, 20, 3, 90)
        // remaining = 2. remainingWorkdays = 3.
        // 3 > 2 * 2 (4) -> false.
        // 2 <= 3 -> true.

        // Assert
        assertEquals("🎯 Quase lá! Apenas mais 2 presenciais e a meta é sua.", result)
    }

    @Test
    fun `given low percentage, when fetchSmartMessage, then return planning message`() = runTest {
        // Arrange
        every { Random.nextInt(100) } returns 10

        // Act
        service.fetchSmartMessage(2, 10, 2, 20)
        // remaining = 8. remainingWorkdays = 2.
        // Alert has priority: 8 > 2.
        // Wait, I need percentage < 30 but not hitting alert.
        
        val result2 = service.fetchSmartMessage(2, 10, 10, 20)
        // remaining = 8. remainingWorkdays = 10. 
        // 10 > 8 -> false (alert)
        // 10 > 8 * 2 (16) -> false (home office)
        // 8 <= 3 -> false (almost there)
        // 20 < 30 -> true.

        // Assert
        assertEquals("🚀 Início de mês! Que tal planejar 2 presenciais para esta semana?", result2)
    }

    @Test
    fun `given standard case, when fetchSmartMessage, then return rhythm message`() = runTest {
        // Arrange
        every { Random.nextInt(100) } returns 10

        // Act
        val result = service.fetchSmartMessage(5, 10, 8, 50)
        // remaining = 5. remainingWorkdays = 8.
        // 8 > 10 -> false.
        // 5 <= 3 -> false.
        // 50 < 30 -> false.

        // Assert
        assertEquals("🎯 Se mantiver o ritmo atual, terminará o mês com 55% da meta.", result)
    }
}
