package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.CheckInSource
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.MonthlySummaryRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.TimeProvider
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
    private val timeProvider = mockk<TimeProvider>()
    private lateinit var useCase: AutoGeofenceCheckInUseCase

    private val today = LocalDate.of(2026, 8, 12)

    @BeforeEach
    fun setup() {
        every { timeProvider.today() } returns today
        every { settingsRepository.settings } returns flowOf(AppSettings())
        coEvery { checkInRepository.getCheckIn(today) } returns null
        coEvery {
            checkInRepository.saveCheckIn(any(), any(), any(), any())
        } returns Unit
        coEvery { monthlySummaryRepository.refreshSummary(any()) } returns Unit
        useCase = AutoGeofenceCheckInUseCase(
            checkInRepository,
            monthlySummaryRepository,
            settingsRepository,
            timeProvider
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
    fun `when non workday, then skip`() = runTest {
        every { timeProvider.today() } returns LocalDate.of(2026, 8, 9) // Sunday

        val result = useCase(workAddressId = 1L)

        assertEquals(AutoCheckInResult.SkippedNonWorkday, result)
        coVerify(exactly = 0) { checkInRepository.saveCheckIn(any(), any(), any(), any()) }
    }
}
