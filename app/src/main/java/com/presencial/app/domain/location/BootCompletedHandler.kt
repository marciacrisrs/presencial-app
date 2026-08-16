package com.presencial.app.domain.location

import com.presencial.app.domain.usecase.SyncGeofencesUseCase
import com.presencial.app.domain.widget.WidgetRefresher
import com.presencial.app.notification.NotificationScheduler
import androidx.work.ExistingWorkPolicy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BootCompletedHandler @Inject constructor(
    private val syncGeofencesUseCase: SyncGeofencesUseCase,
    private val widgetRefresher: WidgetRefresher,
    private val notificationScheduler: NotificationScheduler
) {
    suspend fun handleBootCompleted() {
        notificationScheduler.scheduleDailyReminder()
        notificationScheduler.scheduleWidgetRefresh(ExistingWorkPolicy.REPLACE)
        syncGeofencesUseCase()
        widgetRefresher.refresh()
    }
}
