package com.presencial.app.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NotificationSystemTest {

    private val context: Context = mockk(relaxed = true)
    private val notificationManager: NotificationManager = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var notificationScheduler: NotificationScheduler

    @BeforeEach
    fun setup() {
        every { context.getSystemService(NotificationManager::class.java) } returns notificationManager
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(context) } returns workManager
        
        notificationHelper = NotificationHelper(context)
        notificationScheduler = NotificationScheduler(context)
    }

    @Test
    fun notificationHelper_showCheckInReminder_callsNotify() {
        // Act
        notificationHelper.showCheckInReminder()

        // Assert
        verify { notificationManager.notify(NotificationHelper.NOTIFICATION_ID, any<Notification>()) }
    }

    @Test
    fun notificationHelper_showAutoCheckInNotification_callsNotify() {
        // Act
        notificationHelper.showAutoCheckInNotification()

        // Assert
        verify { notificationManager.notify(NotificationHelper.AUTO_NOTIFICATION_ID, any<Notification>()) }
    }

    @Test
    fun notificationScheduler_scheduleDailyReminder_enqueuesWork() {
        // Act
        notificationScheduler.scheduleDailyReminder()

        // Assert
        verify { 
            workManager.enqueueUniquePeriodicWork(
                "check_in_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                any<PeriodicWorkRequest>()
            )
        }
    }
}
