package com.presencial.app.notification

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.presencial.app.worker.CheckInReminderWorker
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationSystemTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
        runCatching { WorkManagerTestInitHelper.initializeTestWorkManager(context) }
    }

    @Test
    fun notificationHelper_showCheckInReminder_callsNotify() {
        NotificationHelper(context).showCheckInReminder()

        val manager = context.getSystemService(NotificationManager::class.java)
        assertTrue(
            manager.activeNotifications.any { it.id == NotificationHelper.NOTIFICATION_ID }
        )
    }

    @Test
    fun notificationScheduler_scheduleDailyReminder_enqueuesWork() {
        NotificationScheduler(context).scheduleDailyReminder()

        val infos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(CheckInReminderWorker.WORK_NAME)
            .get()
        assertTrue(infos.isNotEmpty())
    }
}
