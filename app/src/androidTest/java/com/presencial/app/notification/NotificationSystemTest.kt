package com.presencial.app.notification

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.presencial.app.worker.CheckInReminderWorker
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationSystemTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        runCatching { WorkManagerTestInitHelper.initializeTestWorkManager(context) }
    }

    @Test
    fun notificationHelper_createChannel_registersChannel() {
        NotificationHelper(context).createChannel()

        val manager = context.getSystemService(NotificationManager::class.java)
        assertNotNull(manager.getNotificationChannel(NotificationHelper.CHANNEL_ID))
    }

    @Test
    fun notificationHelper_showCheckInReminder_doesNotCrash() {
        NotificationHelper(context).showCheckInReminder()
    }

    @Test
    fun notificationHelper_showAutoCheckInNotification_doesNotCrash() {
        NotificationHelper(context).showAutoCheckInNotification()
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
