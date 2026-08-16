package com.presencial.app.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.presencial.app.domain.util.ReminderScheduleCalculator
import com.presencial.app.worker.CheckInReminderWorker
import com.presencial.app.worker.WidgetRefreshWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun scheduleDailyReminder(
        hour: Int = DEFAULT_REMINDER_HOUR,
        minute: Int = DEFAULT_REMINDER_MINUTE
    ) {
        scheduleWidgetRefresh()
        scheduleCheckInReminder(hour, minute)
    }

    fun scheduleWidgetRefresh(policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP) {
        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, WIDGET_REFRESH_HOUR)
            set(java.util.Calendar.MINUTE, WIDGET_REFRESH_MINUTE)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            if (!after(now)) add(java.util.Calendar.DAY_OF_MONTH, 1)
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

    private fun scheduleCheckInReminder(hour: Int, minute: Int) {
        require(hour in 0..23) { "Reminder hour must be between 0 and 23" }
        require(minute in 0..59) { "Reminder minute must be between 0 and 59" }

        val initialDelay = ReminderScheduleCalculator.initialDelayMillis(
            now = LocalDateTime.now(),
            reminderTime = LocalTime.of(hour, minute)
        )

        val workRequest = PeriodicWorkRequestBuilder<CheckInReminderWorker>(
            INTERVAL_HOURS,
            TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CheckInReminderWorker.WORK_NAME,
            androidx.work.ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    companion object {
        const val DEFAULT_REMINDER_HOUR = 18
        const val DEFAULT_REMINDER_MINUTE = 0
        private const val WIDGET_REFRESH_HOUR = 0
        private const val WIDGET_REFRESH_MINUTE = 5
        private const val INTERVAL_HOURS = 24L
    }
}
