package com.presencial.app.domain.usecase

import app.cash.turbine.test
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.util.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetStatisticsUseCaseTest {

    private val checkInRepository: CheckInRepository = mockk()
    private val absenceRepository: AbsenceRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private lateinit var useCase: GetStatisticsUseCase

    @BeforeEach
    fun setup() {
        useCase = GetStatisticsUseCase(
            checkInRepository,
            absenceRepository,
            settingsRepository
        )
    }

    @Test
    fun `given checkins, when invoke, then return correct statistics`() = runTest {
        // Arrange
        val date1 = LocalDate.of(2026, 8, 3) // Monday
        val date2 = LocalDate.of(2026, 8, 4) // Tuesday
        val checkIns = listOf(
            TestDataFactory.createCheckIn(date = date1),
            TestDataFactory.createCheckIn(date = date2)
        )
        val absences = emptyList<com.presencial.app.domain.model.Absence>()
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = false)

        every { checkInRepository.observeAllCheckIns() } returns flowOf(checkIns)
        every { absenceRepository.getAllAbsences() } returns flowOf(absences)
        every { settingsRepository.settings } returns flowOf(settings)

        // Act & Assert
        useCase().test {
            val stats = awaitItem()
            assertEquals(2, stats.totalPresencial)
            assertEquals(2, stats.longestStreak)
            assertEquals(2, stats.currentStreak)
            assertEquals(1, stats.monthlySummaries.size)
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}
