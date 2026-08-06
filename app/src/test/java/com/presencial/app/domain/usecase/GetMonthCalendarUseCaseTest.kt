package com.presencial.app.domain.usecase

import app.cash.turbine.test
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.util.FakeTimeProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import java.time.YearMonth
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetMonthCalendarUseCaseTest {

    private val checkInRepository: CheckInRepository = mockk()
    private val absenceRepository: AbsenceRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val timeProvider = FakeTimeProvider()
    private lateinit var useCase: GetMonthCalendarUseCase

    @BeforeEach
    fun setup() {
        useCase = GetMonthCalendarUseCase(
            checkInRepository,
            absenceRepository,
            settingsRepository,
            timeProvider
        )
    }

    @Test
    fun `given valid month, when invoke, then return list of DayInfo`() = runTest {
        // Arrange
        val yearMonth = YearMonth.of(2026, 8)
        val today = LocalDate.of(2026, 8, 6)
        timeProvider.setToday(today)

        val checkIns = emptyList<com.presencial.app.domain.model.CheckIn>()
        val absences = emptyList<com.presencial.app.domain.model.Absence>()
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = false)

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(checkIns)
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(absences)
        every { settingsRepository.settings } returns flowOf(settings)

        // Act & Assert
        useCase(yearMonth).test {
            val days = awaitItem()
            assertEquals(31, days.size)
            
            val todayDayInfo = days.find { it.date == today }
            assertNotNull(todayDayInfo)
            // Aug 6 2026 is Thursday (Workday)
            assertEquals(DayStatus.HOME_OFFICE, todayDayInfo?.status)

            val futureDay = days.find { it.date == today.plusDays(1) }
            assertEquals(DayStatus.FUTURO, futureDay?.status)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
