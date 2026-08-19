package com.presencial.app.domain.location

import com.presencial.app.notification.GeofenceRestoreScheduler
import com.presencial.app.notification.NotificationScheduler
import androidx.work.ExistingWorkPolicy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BootCompletedHandler @Inject constructor(
    private val geofenceRestoreScheduler: GeofenceRestoreScheduler,
    private val notificationScheduler: NotificationScheduler
) {
    fun handleSystemRestore() {
        notificationScheduler.scheduleDailyReminder()
        notificationScheduler.scheduleWidgetRefresh(ExistingWorkPolicy.REPLACE)
        geofenceRestoreScheduler.scheduleRestore(ExistingWorkPolicy.KEEP)
    }
}
