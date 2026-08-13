package com.presencial.app.domain.usecase

import app.cash.turbine.test
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.TimeProvider
import com.presencial.app.util.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import java.time.YearMonth
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetStatisticsUseCaseTest {

    private val checkInRepository: CheckInRepository = mockk()
    private val absenceRepository: AbsenceRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val getMonthCalendarUseCase: GetMonthCalendarUseCase = mockk()
    private val timeProvider: TimeProvider = mockk()
    private lateinit var useCase: GetStatisticsUseCase

    @BeforeEach
    fun setup() {
        every {
            getMonthCalendarUseCase.buildForMonth(any(), any(), any(), any(), any())
        } returns emptyList()
        every { timeProvider.currentMonth() } returns YearMonth.of(2026, 8)
        useCase = GetStatisticsUseCase(
            checkInRepository,
            absenceRepository,
            settingsRepository,
            getMonthCalendarUseCase,
            timeProvider
        )
    }

    @Test
    fun `given multiple months and streaks, when invoke, then calculate correct statistics`() = runTest {
        val dateAug3 = LocalDate.of(2026, 8, 3)
        val dateAug4 = LocalDate.of(2026, 8, 4)
        val dateAug5 = LocalDate.of(2026, 8, 5)
        val dateAug10 = LocalDate.of(2026, 8, 10)
        val dateJuly31 = LocalDate.of(2026, 7, 31)

        val checkIns = listOf(
            TestDataFactory.createCheckIn(date = dateAug3, status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = dateAug4, status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = dateAug5, status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = dateAug10, status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = dateJuly31, status = DayStatus.PRESENCIAL)
        )

        val absences = listOf(
            TestDataFactory.createAbsence(
                startDate = LocalDate.of(2026, 8, 17),
                endDate = LocalDate.of(2026, 8, 21)
            )
        )

        val settings = AppSettings(
            requiredPercentage = 50,
            countSaturdaysAsWorkdays = false,
            presencePolicy = PresencePolicy.fromLegacyPercentage(50)
        )

        every { checkInRepository.observeAllCheckIns() } returns flowOf(checkIns)
        every { absenceRepository.getAllAbsences() } returns flowOf(absences)
        every { settingsRepository.settings } returns flowOf(settings)

        useCase(2026).test {
            val stats = awaitItem()
            assertEquals(5, stats.totalPresencial)
            assertEquals(3, stats.longestStreak)
            assertEquals(1, stats.currentStreak)
            assertEquals(2026, stats.selectedYear)
            assertTrue(stats.weeklySummaries.isNotEmpty())

            val augSummary = stats.monthlySummaries.find { it.yearMonth == YearMonth.of(2026, 8) }
            assertEquals(16, augSummary?.workdays)
            assertEquals(8, augSummary?.requiredDays)
            assertEquals(4, augSummary?.completedDays)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given streak spanning months, when invoke, then calculate correct streaks`() = runTest {
        val checkIns = listOf(
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 7, 30), status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 7, 31), status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 1), status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 2), status = DayStatus.PRESENCIAL)
        )

        every { checkInRepository.observeAllCheckIns() } returns flowOf(checkIns)
        every { absenceRepository.getAllAbsences() } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings())

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
        val checkIns = listOf(
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 1), status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 2), status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 4), status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 5), status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 6), status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 8), status = DayStatus.PRESENCIAL)
        )

        every { checkInRepository.observeAllCheckIns() } returns flowOf(checkIns)
        every { absenceRepository.getAllAbsences() } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings())

        useCase().test {
            val stats = awaitItem()
            assertEquals(3, stats.longestStreak)
            assertEquals(1, stats.currentStreak)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given unordered checkins, when invoke, then calculate streaks correctly`() = runTest {
        val checkIns = listOf(
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 5), status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 3), status = DayStatus.PRESENCIAL),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 4), status = DayStatus.PRESENCIAL)
        )

        every { checkInRepository.observeAllCheckIns() } returns flowOf(checkIns)
        every { absenceRepository.getAllAbsences() } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings())

        useCase().test {
            val stats = awaitItem()
            assertEquals(3, stats.longestStreak)
            assertEquals(3, stats.currentStreak)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given empty data, when invoke, then return zeroed statistics`() = runTest {
        every { checkInRepository.observeAllCheckIns() } returns flowOf(emptyList())
        every { absenceRepository.getAllAbsences() } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings())

        useCase().test {
            val stats = awaitItem()
            assertEquals(0, stats.totalPresencial)
            assertEquals(0, stats.longestStreak)
            assertEquals(0, stats.currentStreak)
            assertTrue(stats.monthlySummaries.isEmpty())
            assertEquals(0, stats.annualSummary.totalPresencial)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
