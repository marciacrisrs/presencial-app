package com.presencial.app.domain.location

import com.presencial.app.domain.usecase.SyncGeofencesUseCase
import com.presencial.app.domain.widget.WidgetRefresher
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class BootCompletedHandlerTest {

    private val syncGeofencesUseCase = mockk<SyncGeofencesUseCase>(relaxed = true)
    private val widgetRefresher = mockk<WidgetRefresher>(relaxed = true)
    private val handler = BootCompletedHandler(syncGeofencesUseCase, widgetRefresher)

    @Test
    fun `handleBootCompleted should sync geofences and refresh widget`() = runTest {
        handler.handleBootCompleted()

        coVerify(exactly = 1) { syncGeofencesUseCase() }
        coVerify(exactly = 1) { widgetRefresher.refresh() }
    }
}
