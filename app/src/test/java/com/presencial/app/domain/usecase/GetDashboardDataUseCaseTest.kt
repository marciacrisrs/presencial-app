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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
            com.presencial.app.domain.util.SmartMessageFallback(
                com.presencial.app.util.FakeSmartMessageTextProvider()
            ),
            timeProvider
        )
    }

    @Test
    fun `given valid data, when invoke, then return dashboard data with AI message`() = runTest {
        // Arrange
        val yearMonth = YearMonth.of(2026, 8)
        val today = LocalDate.of(2026, 8, 6) // Thursday
        timeProvider.setToday(today)

        val checkIns = listOf(
            TestDataFactory.createCheckIn(date = today)
        )
        val absences = emptyList<com.presencial.app.domain.model.Absence>()
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = false)

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(checkIns)
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(absences)
        every { settingsRepository.settings } returns flowOf(settings)
        coEvery { getAiSmartMessageUseCase(any()) } returns "AI Message"

        // Act & Assert
        useCase(yearMonth).test {
            val firstEmission = awaitItem()
            assertTrue(firstEmission.isLoadingAi)
            assertEquals(1, firstEmission.completedDays)

            val secondEmission = awaitItem()
            assertFalse(secondEmission.isLoadingAi)
            assertEquals("AI Message", secondEmission.smartMessage)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given no check-ins, when invoke, then return zero progress`() = runTest {
        // Arrange
        val yearMonth = YearMonth.of(2026, 8)
        val today = LocalDate.of(2026, 8, 3) // Monday
        timeProvider.setToday(today)

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(emptyList())
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings(40, false))
        coEvery { getAiSmartMessageUseCase(any()) } returns "AI Message"

        // Act & Assert
        useCase(yearMonth).test {
            val dashboard = expectMostRecentItem()
            assertEquals(0, dashboard.completedDays)
            assertEquals(0f, dashboard.progressFraction)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given yesterday was workday and no check-in, when invoke, then yesterdayIsPending should be true`() = runTest {
        // Arrange
        val yearMonth = YearMonth.of(2026, 8)
        val today = LocalDate.of(2026, 8, 4) // Tuesday
        timeProvider.setToday(today)

        // Monday (Aug 3rd) is a workday and has no check-in
        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(emptyList())
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings(40, false))
        coEvery { getAiSmartMessageUseCase(any()) } returns "AI"

        // Act & Assert
        useCase(yearMonth).test {
            val dashboard = expectMostRecentItem()
            assertTrue(dashboard.yesterdayIsPending)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given consecutive check-ins, when invoke, then return correct streak`() = runTest {
        // Arrange
        val yearMonth = YearMonth.of(2026, 8)
        val today = LocalDate.of(2026, 8, 6)
        timeProvider.setToday(today)

        val checkIns = listOf(
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 6)),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 5)),
            TestDataFactory.createCheckIn(date = LocalDate.of(2026, 8, 4))
        )

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(checkIns)
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings(40, false))
        coEvery { getAiSmartMessageUseCase(any()) } returns "AI"

        // Act & Assert
        useCase(yearMonth).test {
            val dashboard = expectMostRecentItem()
            assertEquals(3, dashboard.streak)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given countSaturdaysAsWorkdays is true, when invoke, then workdays should include saturdays`() = runTest {
        // Arrange
        val yearMonth = YearMonth.of(2026, 8)
        timeProvider.setToday(LocalDate.of(2026, 8, 6))

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(emptyList())
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings(40, true))
        coEvery { getAiSmartMessageUseCase(any()) } returns "AI"

        // August 2026 has 5 Saturdays and 21 weekdays (Mon-Fri) = 26 workdays if Saturday is included
        // Wait, let's verify: Aug 1 (Sat), 8, 15, 22, 29. 5 Saturdays.
        // Weekdays: 3, 4, 5, 6, 7 (5), 10-14 (5), 17-21 (5), 24-28 (5), 31 (1) = 21. Total 26.

        // Act & Assert
        useCase(yearMonth).test {
            val dashboard = expectMostRecentItem()
            assertEquals(26, dashboard.workdays)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
