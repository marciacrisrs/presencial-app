package com.presencial.app.domain.usecase

import app.cash.turbine.test
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.util.FakeTimeProvider
import com.presencial.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import java.time.YearMonth
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetDashboardDataUseCaseTest {

    private val checkInRepository: CheckInRepository = mockk()
    private val absenceRepository: AbsenceRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val getAiSmartMessageUseCase: GetAiSmartMessageUseCase = mockk()
    private val timeProvider = FakeTimeProvider()
    private lateinit var useCase: GetDashboardDataUseCase

    @BeforeEach
    fun setup() {
        useCase = GetDashboardDataUseCase(
            checkInRepository,
            absenceRepository,
            settingsRepository,
            getAiSmartMessageUseCase,
            timeProvider
        )
    }

    @Test
    fun `given valid data, when invoke, then return dashboard data with AI message`() = runTest {
        // Arrange
        val yearMonth = YearMonth.of(2026, 8)
        val today = LocalDate.of(2026, 8, 6)
        timeProvider.setToday(today)

        val checkIns = listOf(
            TestDataFactory.createCheckIn(date = today)
        )
        val absences = emptyList<com.presencial.app.domain.model.Absence>()
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = false)

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(checkIns)
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(absences)
        every { settingsRepository.settings } returns flowOf(settings)
        coEvery { getAiSmartMessageUseCase(any(), any(), any(), any(), any(), any(), any()) } returns "AI Message"

        // Act & Assert
        useCase(yearMonth).test {
            val firstEmission = awaitItem()
            assertTrue(firstEmission.isLoadingAi)

            val secondEmission = awaitItem()
            assertFalse(secondEmission.isLoadingAi)
            assertEquals("AI Message", secondEmission.smartMessage)
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}
