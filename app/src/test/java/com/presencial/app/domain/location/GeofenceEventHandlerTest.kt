package com.presencial.app.domain.location

import com.presencial.app.domain.usecase.AutoCheckInResult
import com.presencial.app.domain.usecase.AutoGeofenceCheckInUseCase
import com.presencial.app.notification.NotificationHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GeofenceEventHandlerTest {

    private val autoGeofenceCheckInUseCase = mockk<AutoGeofenceCheckInUseCase>()
    private val notificationHelper = mockk<NotificationHelper>(relaxed = true)
    private lateinit var handler: GeofenceEventHandler

    @BeforeEach
    fun setup() {
        handler = GeofenceEventHandler(autoGeofenceCheckInUseCase, notificationHelper)
    }

    @Test
    fun `when check-in succeeds, then show notification`() = runTest {
        coEvery { autoGeofenceCheckInUseCase(7L) } returns AutoCheckInResult.Success

        val result = handler.handleDwellTransition(7L)

        assertEquals(AutoCheckInResult.Success, result)
        coVerify { autoGeofenceCheckInUseCase(7L) }
        coVerify { notificationHelper.showAutoCheckInNotification() }
    }

    @Test
    fun `when already checked in, then skip notification`() = runTest {
        coEvery { autoGeofenceCheckInUseCase(1L) } returns AutoCheckInResult.SkippedAlreadyCheckedIn

        val result = handler.handleDwellTransition(1L)

        assertEquals(AutoCheckInResult.SkippedAlreadyCheckedIn, result)
        coVerify(exactly = 0) { notificationHelper.showAutoCheckInNotification() }
    }

    @Test
    fun `when non workday, then skip notification`() = runTest {
        coEvery { autoGeofenceCheckInUseCase(null) } returns AutoCheckInResult.SkippedNonWorkday

        val result = handler.handleDwellTransition(null)

        assertEquals(AutoCheckInResult.SkippedNonWorkday, result)
        coVerify(exactly = 0) { notificationHelper.showAutoCheckInNotification() }
    }

    @Test
    fun `when absence covers today, then skip notification`() = runTest {
        coEvery { autoGeofenceCheckInUseCase(1L) } returns AutoCheckInResult.SkippedAbsence

        val result = handler.handleDwellTransition(1L)

        assertEquals(AutoCheckInResult.SkippedAbsence, result)
        coVerify(exactly = 0) { notificationHelper.showAutoCheckInNotification() }
    }
}
