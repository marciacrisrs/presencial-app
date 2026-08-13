package com.presencial.app.domain.usecase

import app.cash.turbine.test
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.DayInfo
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.repository.AbsenceRepository
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

class GetAttendanceReportUseCaseTest {

    private val getMonthCalendarUseCase: GetMonthCalendarUseCase = mockk()
    private val absenceRepository: AbsenceRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val timeProvider = FakeTimeProvider(LocalDate.of(2026, 8, 15))
    private lateinit var useCase: GetAttendanceReportUseCase

    @BeforeEach
    fun setup() {
        useCase = GetAttendanceReportUseCase(
            getMonthCalendarUseCase,
            absenceRepository,
            settingsRepository,
            timeProvider
        )
    }

    @Test
    fun `given month data, when invoke, then build rows and footer`() = runTest {
        val yearMonth = YearMonth.of(2026, 8)
        val days = listOf(
            day(LocalDate.of(2026, 8, 3), DayStatus.PRESENCIAL, isWorkday = true),
            day(LocalDate.of(2026, 8, 4), DayStatus.PRESENCIAL, isWorkday = true),
            day(LocalDate.of(2026, 8, 5), DayStatus.HOME_OFFICE, isWorkday = true),
            day(
                date = LocalDate.of(2026, 8, 7),
                status = DayStatus.FERIADO,
                isWorkday = false,
                isHoliday = true,
                holidayName = "Independência"
            ),
            day(LocalDate.of(2026, 8, 9), DayStatus.FIM_DE_SEMANA, isWorkday = false),
            day(LocalDate.of(2026, 8, 10), DayStatus.FALTOU, isWorkday = true)
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

        every { getMonthCalendarUseCase(yearMonth) } returns flowOf(days)
        every {
            absenceRepository.getAbsencesInRange(
                yearMonth.atDay(1),
                yearMonth.atEndOfMonth()
            )
        } returns flowOf(absences)
        every { settingsRepository.settings } returns flowOf(settings)

        useCase(yearMonth).test {
            val report = awaitItem()

            assertEquals(yearMonth, report.yearMonth)
            assertEquals(6, report.rows.size)

            val presencial = report.rows.first { it.date == LocalDate.of(2026, 8, 3) }
            assertEquals("Presencial", presencial.statusLabel)
            assertTrue(presencial.isWorkday)
            assertFalse(presencial.isHoliday)

            val holiday = report.rows.first { it.date == LocalDate.of(2026, 8, 7) }
            assertEquals("Feriado (Independência)", holiday.statusLabel)
            assertTrue(holiday.isHoliday)
            assertEquals("Independência", holiday.holidayName)

            assertEquals(16, report.footer.workdays)
            assertEquals(8, report.footer.requiredDays)
            assertEquals(2, report.footer.completedDays)
            assertEquals(50, report.footer.requiredPercentage)
            assertEquals(25f, report.footer.achievedPercentage)
            assertEquals(LocalDate.of(2026, 8, 15), report.footer.exportedAt)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given empty month, when invoke, then return zeroed footer`() = runTest {
        val yearMonth = YearMonth.of(2026, 1)
        val settings = AppSettings()

        every { getMonthCalendarUseCase(yearMonth) } returns flowOf(emptyList())
        every {
            absenceRepository.getAbsencesInRange(
                yearMonth.atDay(1),
                yearMonth.atEndOfMonth()
            )
        } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(settings)

        useCase(yearMonth).test {
            val report = awaitItem()

            assertTrue(report.rows.isEmpty())
            assertEquals(21, report.footer.workdays)
            assertEquals(0, report.footer.completedDays)
            assertEquals(0f, report.footer.achievedPercentage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun day(
        date: LocalDate,
        status: DayStatus,
        isWorkday: Boolean,
        isHoliday: Boolean = false,
        holidayName: String? = null
    ) = DayInfo(
        date = date,
        status = status,
        isWorkday = isWorkday,
        isHoliday = isHoliday,
        holidayName = holidayName
    )
}
