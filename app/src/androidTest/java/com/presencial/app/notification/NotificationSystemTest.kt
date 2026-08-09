package com.presencial.app.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationSystemTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val notificationManager: NotificationManager = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var notificationScheduler: NotificationScheduler

    @Before
    fun setup() {
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManager
        
        notificationHelper = NotificationHelper(context)
        notificationScheduler = NotificationScheduler(context)
    }

    @After
    fun tearDown() {
        unmockkStatic(WorkManager::class)
    }

    @Test
    fun notificationHelper_showCheckInReminder_callsNotify() {
        val mockContext: Context = mockk(relaxed = true)
        every { mockContext.getSystemService(NotificationManager::class.java) } returns notificationManager
        every { mockContext.applicationContext } returns mockContext
        every { mockContext.getString(any()) } returns "test"
        
        val helper = NotificationHelper(mockContext)
        helper.showCheckInReminder()

        verify { notificationManager.notify(NotificationHelper.NOTIFICATION_ID, any<Notification>()) }
    }

    @Test
    fun notificationScheduler_scheduleDailyReminder_enqueuesWork() {
        notificationScheduler.scheduleDailyReminder()

        verify { 
            workManager.enqueueUniquePeriodicWork(
                "check_in_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                any<PeriodicWorkRequest>()
            )
        }
    }
}
