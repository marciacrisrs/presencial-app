package com.presencial.app.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.presencial.app.worker.CheckInReminderWorker
import com.presencial.app.worker.WidgetRefreshWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun scheduleDailyReminder() {
        scheduleWidgetRefresh()
        scheduleCheckInReminder()
    }

    fun scheduleWidgetRefresh(policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, WIDGET_REFRESH_HOUR)
            set(Calendar.MINUTE, WIDGET_REFRESH_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_MONTH, 1)
        }
        val initialDelay = (target.timeInMillis - now.timeInMillis).coerceAtLeast(1L)

        val workRequest = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WidgetRefreshWorker.WORK_NAME,
            policy,
            workRequest
        )
    }

    private fun scheduleCheckInReminder() {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, TARGET_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
        }
        val initialDelay = target.timeInMillis - now.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<CheckInReminderWorker>(
            INTERVAL_HOURS, 
            TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CheckInReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    companion object {
        private const val TARGET_HOUR = 18
        private const val WIDGET_REFRESH_HOUR = 0
        private const val WIDGET_REFRESH_MINUTE = 5
        private const val INTERVAL_HOURS = 24L
    }
}
