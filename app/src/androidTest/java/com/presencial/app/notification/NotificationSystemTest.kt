package com.presencial.app.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.presencial.app.worker.CheckInReminderWorker
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
    fun notificationHelper_showCheckInReminder_callsNotify() {
        val notificationManager = mockk<NotificationManager>(relaxed = true)
        every { notificationManager.areNotificationsEnabled() } returns true
        val helperContext = NotificationTestContext(context, notificationManager)

        NotificationHelper(helperContext).showCheckInReminder()

        verify { notificationManager.notify(NotificationHelper.NOTIFICATION_ID, any<Notification>()) }
    }

    @Test
    fun notificationScheduler_scheduleDailyReminder_enqueuesWork() {
        NotificationScheduler(context).scheduleDailyReminder()

        val infos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(CheckInReminderWorker.WORK_NAME)
            .get()
        assertTrue(infos.isNotEmpty())
    }

    private class NotificationTestContext(
        base: Context,
        private val notificationManager: NotificationManager
    ) : ContextWrapper(base) {
        override fun getApplicationContext(): Context = this

        override fun checkSelfPermission(permission: String): Int =
            PackageManager.PERMISSION_GRANTED

        override fun getSystemService(name: String): Any? =
            if (name == Context.NOTIFICATION_SERVICE) notificationManager
            else super.getSystemService(name)
    }
}
