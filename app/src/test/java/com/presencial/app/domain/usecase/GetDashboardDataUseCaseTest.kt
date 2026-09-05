package com.presencial.app.domain.usecase

import app.cash.turbine.test
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.util.FakeTimeProvider
import com.presencial.app.util.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import java.time.YearMonth
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetDashboardDataUseCaseTest {

    private val checkInRepository: CheckInRepository = mockk()
    private val absenceRepository: AbsenceRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val getSmartMessageUseCase: GetSmartMessageUseCase = mockk()
    private val timeProvider = FakeTimeProvider()
    private lateinit var useCase: GetDashboardDataUseCase

    @BeforeEach
    fun setup() {
        useCase = GetDashboardDataUseCase(
            checkInRepository,
            absenceRepository,
            settingsRepository,
            getSmartMessageUseCase,
            timeProvider
        )
        every { getSmartMessageUseCase(any()) } returns "Mensagem contextual"
    }

    @Test
    fun `given valid data, when invoke, then return dashboard data with smart message`() = runTest {
        val yearMonth = YearMonth.of(2026, 8)
        val today = LocalDate.of(2026, 8, 6)
        timeProvider.setToday(today)

        val checkIns = listOf(TestDataFactory.createCheckIn(date = today))
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = false)

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(checkIns)
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(settings)

        useCase(yearMonth).test {
            val dashboard = awaitItem()
            assertEquals(1, dashboard.completedDays)
            assertEquals("Mensagem contextual", dashboard.smartMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given no check-ins, when invoke, then return zero progress`() = runTest {
        val yearMonth = YearMonth.of(2026, 8)
        timeProvider.setToday(LocalDate.of(2026, 8, 3))

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(emptyList())
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings(40, false))

        useCase(yearMonth).test {
            val dashboard = awaitItem()
            assertEquals(0, dashboard.completedDays)
            assertEquals(0f, dashboard.progressFraction)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given yesterday was workday and no check-in, when invoke, then yesterdayIsPending should be true`() = runTest {
        val yearMonth = YearMonth.of(2026, 8)
        timeProvider.setToday(LocalDate.of(2026, 8, 4))

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(emptyList())
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings(40, false))

        useCase(yearMonth).test {
            val dashboard = awaitItem()
            assertTrue(dashboard.yesterdayIsPending)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given consecutive check-ins, when invoke, then return correct streak`() = runTest {
        val yearMonth = YearMonth.of(2026, 8)
        timeProvider.setToday(LocalDate.of(2026, 8, 6))

        val checkIns = listOf(
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 6)),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 5)),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 4))
        )

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(checkIns)
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings(40, false))

        useCase(yearMonth).test {
            val dashboard = awaitItem()
            assertEquals(3, dashboard.streak)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given countSaturdaysAsWorkdays is true, when invoke, then workdays should include saturdays`() = runTest {
        val yearMonth = YearMonth.of(2026, 8)
        timeProvider.setToday(LocalDate.of(2026, 8, 6))

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(emptyList())
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings(40, true))

        useCase(yearMonth).test {
            val dashboard = awaitItem()
            assertEquals(26, dashboard.workdays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given first of month with yesterday presencial, when invoke, then yesterdayIsPending is false`() = runTest {
        val today = LocalDate.of(2026, 9, 1)
        val yesterday = today.minusDays(1)
        val yearMonth = YearMonth.from(today)
        timeProvider.setToday(today)

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(emptyList())
        every { checkInRepository.observeCheckInsForMonth(YearMonth.from(yesterday)) } returns flowOf(
            listOf(TestDataFactory.createCheckIn(date = yesterday, status = DayStatus.PRESENCIAL))
        )
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings(40, false))

        useCase(yearMonth).test {
            val dashboard = awaitItem()
            assertFalse(dashboard.yesterdayIsPending)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given first of month with yesterday home office, when invoke, then yesterdayIsPending is false`() = runTest {
        val today = LocalDate.of(2026, 9, 1)
        val yesterday = today.minusDays(1)
        val yearMonth = YearMonth.from(today)
        timeProvider.setToday(today)

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(emptyList())
        every { checkInRepository.observeCheckInsForMonth(YearMonth.from(yesterday)) } returns flowOf(
            listOf(TestDataFactory.createCheckIn(date = yesterday, status = DayStatus.HOME_OFFICE))
        )
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings(40, false))

        useCase(yearMonth).test {
            val dashboard = awaitItem()
            assertFalse(dashboard.yesterdayIsPending)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given first of month with no yesterday check-in, when invoke, then yesterdayIsPending is true`() = runTest {
        val today = LocalDate.of(2026, 9, 1)
        val yesterday = today.minusDays(1)
        val yearMonth = YearMonth.from(today)
        timeProvider.setToday(today)

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(emptyList())
        every { checkInRepository.observeCheckInsForMonth(YearMonth.from(yesterday)) } returns flowOf(emptyList())
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings(40, false))

        useCase(yearMonth).test {
            val dashboard = awaitItem()
            assertTrue(dashboard.yesterdayIsPending)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given first of month with yesterday absence, when invoke, then yesterdayIsPending is false`() = runTest {
        val today = LocalDate.of(2026, 9, 1)
        val yesterday = today.minusDays(1)
        val yearMonth = YearMonth.from(today)
        timeProvider.setToday(today)

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(emptyList())
        every { checkInRepository.observeCheckInsForMonth(YearMonth.from(yesterday)) } returns flowOf(emptyList())
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(
            listOf(TestDataFactory.createAbsence(startDate = yesterday, endDate = yesterday))
        )
        every { settingsRepository.settings } returns flowOf(AppSettings(40, false))

        useCase(yearMonth).test {
            val dashboard = awaitItem()
            assertFalse(dashboard.yesterdayIsPending)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
