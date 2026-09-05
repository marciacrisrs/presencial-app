package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.CheckInSource
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.MonthlySummaryRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.TimeProvider
import com.presencial.app.domain.widget.WidgetRefresher
import com.presencial.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class AutoGeofenceCheckInUseCaseTest {

    private val checkInRepository = mockk<CheckInRepository>()
    private val monthlySummaryRepository = mockk<MonthlySummaryRepository>()
    private val settingsRepository = mockk<SettingsRepository>()
    private val absenceRepository = mockk<AbsenceRepository>()
    private val timeProvider = mockk<TimeProvider>()
    private val widgetRefresher = mockk<WidgetRefresher>()
    private lateinit var useCase: AutoGeofenceCheckInUseCase

    private val today = LocalDate.of(2026, 8, 12)

    @BeforeEach
    fun setup() {
        every { timeProvider.today() } returns today
        every { settingsRepository.settings } returns flowOf(AppSettings())
        every { absenceRepository.getAbsencesInRange(today, today) } returns flowOf(emptyList())
        coEvery { checkInRepository.getCheckIn(today) } returns null
        coEvery {
            checkInRepository.saveCheckIn(any(), any(), any(), any())
        } returns Unit
        coEvery { monthlySummaryRepository.refreshSummary(any()) } returns Unit
        coEvery { widgetRefresher.refresh() } returns Unit
        useCase = AutoGeofenceCheckInUseCase(
            checkInRepository,
            monthlySummaryRepository,
            settingsRepository,
            absenceRepository,
            timeProvider,
            widgetRefresher
        )
    }

    @Test
    fun `when workday and not checked in, then save auto geofence check-in`() = runTest {
        val result = useCase(workAddressId = 7L)

        assertEquals(AutoCheckInResult.Success, result)
        coVerify {
            checkInRepository.saveCheckIn(
                today,
                DayStatus.PRESENCIAL,
                CheckInSource.AUTO_GEOFENCE,
                7L
            )
        }
        coVerify { monthlySummaryRepository.refreshSummary(YearMonth.from(today)) }
        coVerify { widgetRefresher.refresh() }
    }

    @Test
    fun `when already checked in, then skip`() = runTest {
        coEvery { checkInRepository.getCheckIn(today) } returns TestDataFactory.createCheckIn(
            date = today,
            status = DayStatus.PRESENCIAL
        )

        val result = useCase(workAddressId = 1L)

        assertEquals(AutoCheckInResult.SkippedAlreadyCheckedIn, result)
        coVerify(exactly = 0) { checkInRepository.saveCheckIn(any(), any(), any(), any()) }
    }

    @Test
    fun `when home office is already recorded, then skip`() = runTest {
        coEvery { checkInRepository.getCheckIn(today) } returns TestDataFactory.createCheckIn(
            date = today,
            status = DayStatus.HOME_OFFICE
        )

        val result = useCase(workAddressId = 1L)

        assertEquals(AutoCheckInResult.SkippedAlreadyCheckedIn, result)
        coVerify(exactly = 0) { checkInRepository.saveCheckIn(any(), any(), any(), any()) }
    }

    @Test
    fun `when full-day absence covers today, then skip`() = runTest {
        every { absenceRepository.getAbsencesInRange(today, today) } returns flowOf(
            listOf(
                TestDataFactory.createAbsence(
                    startDate = today.minusDays(1),
                    endDate = today.plusDays(1),
                    isFullDay = true
                )
            )
        )

        val result = useCase(workAddressId = 1L)

        assertEquals(AutoCheckInResult.SkippedAbsence, result)
        coVerify(exactly = 0) { checkInRepository.saveCheckIn(any(), any(), any(), any()) }
    }

    @Test
    fun `when only partial-day absence covers today, then save auto geofence check-in`() = runTest {
        every { absenceRepository.getAbsencesInRange(today, today) } returns flowOf(
            listOf(
                TestDataFactory.createAbsence(
                    startDate = today,
                    endDate = today,
                    isFullDay = false,
                    hours = 4f
                )
            )
        )

        val result = useCase(workAddressId = 3L)

        assertEquals(AutoCheckInResult.Success, result)
        coVerify {
            checkInRepository.saveCheckIn(
                today,
                DayStatus.PRESENCIAL,
                CheckInSource.AUTO_GEOFENCE,
                3L
            )
        }
    }

    @Test
    fun `when non workday, then skip`() = runTest {
        every { timeProvider.today() } returns LocalDate.of(2026, 8, 9) // Sunday

        val result = useCase(workAddressId = 1L)

        assertEquals(AutoCheckInResult.SkippedNonWorkday, result)
        coVerify(exactly = 0) { checkInRepository.saveCheckIn(any(), any(), any(), any()) }
    }
}
