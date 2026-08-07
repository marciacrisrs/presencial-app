package com.presencial.app.domain.usecase

import app.cash.turbine.test
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.util.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
    fun `given multiple months and streaks, when invoke, then calculate correct statistics`() = runTest {
        // Arrange
        val dateAug3 = LocalDate.of(2026, 8, 3)  // Mon
        val dateAug4 = LocalDate.of(2026, 8, 4)  // Tue
        val dateAug5 = LocalDate.of(2026, 8, 5)  // Wed
        val dateAug10 = LocalDate.of(2026, 8, 10) // Mon
        val dateJuly31 = LocalDate.of(2026, 7, 31) // Fri

        val checkIns = listOf(
            TestDataFactory.createCheckIn(date = dateAug3, status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = dateAug4, status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = dateAug5, status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = dateAug10, status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = dateJuly31, status = DayStatus.PRESENCIAL)
        )
        // Streak Aug 3-5 is 3. Aug 10 is 1. July 31 is 1.
        // Longest streak: 3.
        // Current streak (sorted descending): Aug 10 -> break at Aug 9. Current streak is 1.
        // Wait, if today is Aug 10, current streak is 1. If today was Aug 5, it would be 3.
        
        val absences = listOf(
            TestDataFactory.createAbsence(
                startDate = LocalDate.of(2026, 8, 17),
                endDate = LocalDate.of(2026, 8, 21)
            )
        )
        // 5 workdays subtracted from Aug 2026.
        // Aug 2026 has 21 workdays - 5 = 16 liquid workdays.
        
        val settings = AppSettings(requiredPercentage = 50, countSaturdaysAsWorkdays = false)

        every { checkInRepository.observeAllCheckIns() } returns flowOf(checkIns)
        every { absenceRepository.getAllAbsences() } returns flowOf(absences)
        every { settingsRepository.settings } returns flowOf(settings)

        // Act & Assert
        useCase().test {
            val stats = awaitItem()
            assertEquals(5, stats.totalPresencial)
            assertEquals(3, stats.longestStreak)
            assertEquals(1, stats.currentStreak) 
            // Aug 10 is the latest, but Aug 9 was Sunday (not in checkins anyway), Aug 8 Sat. Aug 7 Fri (missing).
            
            val augSummary = stats.monthlySummaries.find { it.yearMonth == java.time.YearMonth.of(2026, 8) }
            assertEquals(16, augSummary?.workdays)
            assertEquals(8, augSummary?.requiredDays) // 50% of 16
            assertEquals(4, augSummary?.completedDays)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given streak spanning months, when invoke, then calculate correct streaks`() = runTest {
        // Arrange
        val checkIns = listOf(
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 7, 30), status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 7, 31), status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 1), status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 2), status = DayStatus.PRESENCIAL)
        )
        // Streak: July 30, 31, Aug 1, 2. Total 4.

        every { checkInRepository.observeAllCheckIns() } returns flowOf(checkIns)
        every { absenceRepository.getAllAbsences() } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings())

        // Act & Assert
        useCase().test {
            val stats = awaitItem()
            assertEquals(4, stats.totalPresencial)
            assertEquals(4, stats.longestStreak)
            assertEquals(4, stats.currentStreak)
            assertEquals(2, stats.monthlySummaries.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given broken streaks, when invoke, then calculate longest correctly`() = runTest {
        // Arrange
        val checkIns = listOf(
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 1), status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 2), status = DayStatus.PRESENCIAL),
            // Gap Aug 3
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 4), status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 5), status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 6), status = DayStatus.PRESENCIAL),
            // Gap Aug 7
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 8), status = DayStatus.PRESENCIAL)
        )

        every { checkInRepository.observeAllCheckIns() } returns flowOf(checkIns)
        every { absenceRepository.getAllAbsences() } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings())

        // Act & Assert
        useCase().test {
            val stats = awaitItem()
            assertEquals(3, stats.longestStreak) // Aug 4-6
            assertEquals(1, stats.currentStreak) // Aug 8 (if today is Aug 8)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given unordered checkins, when invoke, then calculate streaks correctly`() = runTest {
        // Arrange
        val checkIns = listOf(
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 5), status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 3), status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 4), status = DayStatus.PRESENCIAL)
        )

        every { checkInRepository.observeAllCheckIns() } returns flowOf(checkIns)
        every { absenceRepository.getAllAbsences() } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings())

        // Act & Assert
        useCase().test {
            val stats = awaitItem()
            assertEquals(3, stats.longestStreak)
            assertEquals(3, stats.currentStreak)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given empty data, when invoke, then return zeroed statistics`() = runTest {
        // Arrange
        every { checkInRepository.observeAllCheckIns() } returns flowOf(emptyList())
        every { absenceRepository.getAllAbsences() } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings())

        // Act & Assert
        useCase().test {
            val stats = awaitItem()
            assertEquals(0, stats.totalPresencial)
            assertEquals(0, stats.longestStreak)
            assertEquals(0, stats.currentStreak)
            assertTrue(stats.monthlySummaries.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
