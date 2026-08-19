package com.presencial.app.domain.location

import com.presencial.app.notification.GeofenceRestoreScheduler
import com.presencial.app.notification.NotificationScheduler
import androidx.work.ExistingWorkPolicy
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class BootCompletedHandlerTest {

    private val geofenceRestoreScheduler = mockk<GeofenceRestoreScheduler>(relaxed = true)
    private val notificationScheduler = mockk<NotificationScheduler>(relaxed = true)
    private val handler = BootCompletedHandler(
        geofenceRestoreScheduler,
        notificationScheduler
    )

    @Test
    fun `handleSystemRestore schedules reminders and deduplicated geofence restore`() {
        handler.handleSystemRestore()

        verify(exactly = 1) { notificationScheduler.scheduleDailyReminder() }
        verify(exactly = 1) {
            notificationScheduler.scheduleWidgetRefresh(ExistingWorkPolicy.REPLACE)
        }
        verify(exactly = 1) {
            geofenceRestoreScheduler.scheduleRestore(ExistingWorkPolicy.KEEP)
        }
    }
}
