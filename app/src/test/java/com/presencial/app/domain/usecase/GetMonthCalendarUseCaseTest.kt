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
import org.junit.jupiter.api.Assertions.assertEquals
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
    fun `given valid month, when invoke, then return list of DayInfo with correct statuses`() = runTest {
        // Arrange
        val yearMonth = YearMonth.of(2026, 9) // September has Sept 7 holiday
        val today = LocalDate.of(2026, 9, 10)
        timeProvider.setToday(today)

        val checkIns = listOf(
            com.presencial.app.domain.model.CheckIn(
                LocalDate.of(2026, 9, 1),
                DayStatus.PRESENCIAL
            )
        )
        val absences = listOf(
            com.presencial.app.domain.model.Absence(
                1L,
                com.presencial.app.domain.model.AbsenceType.VACATION,
                LocalDate.of(2026, 9, 14),
                LocalDate.of(2026, 9, 15),
                true
            )
        )
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = false)

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(checkIns)
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(absences)
        every { settingsRepository.settings } returns flowOf(settings)

        // Act & Assert
        useCase(yearMonth).test {
            val days = awaitItem()
            assertEquals(30, days.size)
            
            // Sept 1: Presencial (from checkIns)
            assertEquals(DayStatus.PRESENCIAL, days.find { it.date == LocalDate.of(2026, 9, 1) }?.status)

            // Sept 6: Sunday (FIM_DE_SEMANA)
            assertEquals(DayStatus.FIM_DE_SEMANA, days.find { it.date == LocalDate.of(2026, 9, 6) }?.status)

            // Sept 7: Holiday (FERIADO)
            val sept7 = days.find { it.date == LocalDate.of(2026, 9, 7) }
            assertEquals(DayStatus.FERIADO, sept7?.status)
            assertEquals("Independência", sept7?.holidayName)

            // Sept 8: Workday before today without check-in (FALTOU)
            assertEquals(DayStatus.FALTOU, days.find { it.date == LocalDate.of(2026, 9, 8) }?.status)

            // Sept 10: Today without check-in (HOME_OFFICE)
            assertEquals(DayStatus.HOME_OFFICE, days.find { it.date == LocalDate.of(2026, 9, 10) }?.status)

            // Sept 14: Absence (ABSENCE)
            assertEquals(DayStatus.ABSENCE, days.find { it.date == LocalDate.of(2026, 9, 14) }?.status)

            // Sept 20: Future (FUTURO)
            assertEquals(DayStatus.FUTURO, days.find { it.date == LocalDate.of(2026, 9, 20) }?.status)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given check-in on Sunday, when invoke, then status should be PRESENCIAL instead of FIM_DE_SEMANA`() = runTest {
        // Arrange
        val yearMonth = YearMonth.of(2026, 8)
        val sunday = LocalDate.of(2026, 8, 2) // Sunday
        timeProvider.setToday(LocalDate.of(2026, 8, 3))

        val checkIns = listOf(
            com.presencial.app.domain.model.CheckIn(sunday, DayStatus.PRESENCIAL)
        )
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = false)

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(checkIns)
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(settings)

        // Act & Assert
        useCase(yearMonth).test {
            val days = awaitItem()
            val sundayInfo = days.find { it.date == sunday }
            assertEquals(DayStatus.PRESENCIAL, sundayInfo?.status, "Sunday check-in should be PRESENCIAL")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given check-in on Saturday, when invoke, then status should be PRESENCIAL even if not workday`() = runTest {
        // Arrange
        val yearMonth = YearMonth.of(2026, 8)
        val saturday = LocalDate.of(2026, 8, 1) // Saturday
        timeProvider.setToday(LocalDate.of(2026, 8, 3))

        val checkIns = listOf(
            com.presencial.app.domain.model.CheckIn(saturday, DayStatus.PRESENCIAL)
        )
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = false)

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(checkIns)
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(settings)

        // Act & Assert
        useCase(yearMonth).test {
            val days = awaitItem()
            val satInfo = days.find { it.date == saturday }
            assertEquals(DayStatus.PRESENCIAL, satInfo?.status, "Saturday check-in should be PRESENCIAL")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given holiday with check-in, when invoke, then status should be PRESENCIAL instead of FERIADO`() = runTest {
        // Arrange
        val yearMonth = YearMonth.of(2026, 9)
        val holiday = LocalDate.of(2026, 9, 7) // Independência
        timeProvider.setToday(LocalDate.of(2026, 9, 10))

        val checkIns = listOf(
            com.presencial.app.domain.model.CheckIn(holiday, DayStatus.PRESENCIAL)
        )

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(checkIns)
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(AppSettings(40, false))

        // Act & Assert
        useCase(yearMonth).test {
            val days = awaitItem()
            val holidayInfo = days.find { it.date == holiday }
            assertEquals(DayStatus.PRESENCIAL, holidayInfo?.status, "Holiday check-in should be PRESENCIAL")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given saturdays as workdays, when invoke, then saturdays are not weekends`() = runTest {
        // Arrange
        val yearMonth = YearMonth.of(2026, 8)
        val saturday = LocalDate.of(2026, 8, 8)
        timeProvider.setToday(LocalDate.of(2026, 8, 1))

        val checkIns = emptyList<com.presencial.app.domain.model.CheckIn>()
        val absences = emptyList<com.presencial.app.domain.model.Absence>()
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = true)

        every { checkInRepository.observeCheckInsForMonth(yearMonth) } returns flowOf(checkIns)
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(absences)
        every { settingsRepository.settings } returns flowOf(settings)

        // Act & Assert
        useCase(yearMonth).test {
            val days = awaitItem()
            val satInfo = days.find { it.date == saturday }
            assertEquals(false, satInfo?.status == DayStatus.FIM_DE_SEMANA)
            assertEquals(true, satInfo?.isWorkday)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
