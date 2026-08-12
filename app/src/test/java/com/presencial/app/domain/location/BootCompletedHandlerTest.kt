package com.presencial.app.domain.location

import com.presencial.app.domain.usecase.SyncGeofencesUseCase
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class BootCompletedHandlerTest {

    private val syncGeofencesUseCase = mockk<SyncGeofencesUseCase>(relaxed = true)
    private val handler = BootCompletedHandler(syncGeofencesUseCase)

    @Test
    fun `handleBootCompleted should sync geofences`() = runTest {
        handler.handleBootCompleted()

        coVerify(exactly = 1) { syncGeofencesUseCase() }
    }
}
