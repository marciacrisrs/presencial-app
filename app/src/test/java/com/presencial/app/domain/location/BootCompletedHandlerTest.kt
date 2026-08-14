package com.presencial.app.domain.location

import com.presencial.app.domain.usecase.SyncGeofencesUseCase
import com.presencial.app.domain.widget.WidgetRefresher
import com.presencial.app.notification.NotificationScheduler
import androidx.work.ExistingWorkPolicy
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class BootCompletedHandlerTest {

    private val syncGeofencesUseCase = mockk<SyncGeofencesUseCase>(relaxed = true)
    private val widgetRefresher = mockk<WidgetRefresher>(relaxed = true)
    private val notificationScheduler = mockk<NotificationScheduler>(relaxed = true)
    private val handler = BootCompletedHandler(
        syncGeofencesUseCase,
        widgetRefresher,
        notificationScheduler
    )

    @Test
    fun `handleBootCompleted should sync geofences and refresh widget`() = runTest {
        handler.handleBootCompleted()

        verify(exactly = 1) {
            notificationScheduler.scheduleWidgetRefresh(ExistingWorkPolicy.REPLACE)
        }
        coVerify(exactly = 1) { syncGeofencesUseCase() }
        coVerify(exactly = 1) { widgetRefresher.refresh() }
    }
}
