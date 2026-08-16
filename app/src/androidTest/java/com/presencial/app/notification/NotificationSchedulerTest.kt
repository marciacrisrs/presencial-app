package com.presencial.app.notification

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.presencial.app.worker.CheckInReminderWorker
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationSchedulerTest {

    private lateinit var workManager: WorkManager
    private lateinit var scheduler: NotificationScheduler

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        workManager = WorkManager.getInstance(context)
        scheduler = NotificationScheduler(context)
    }

    @After
    fun tearDown() {
        workManager.cancelAllWork().result.get()
    }

    @Test
    fun reschedulingReminderReplacesPreviousPeriodicWork() {
        scheduler.scheduleDailyReminder(hour = 18, minute = 0)
        val first = workManager
            .getWorkInfosForUniqueWork(CheckInReminderWorker.WORK_NAME)
            .get()
            .single()

        scheduler.scheduleDailyReminder(hour = 19, minute = 0)
        val current = workManager
            .getWorkInfosForUniqueWork(CheckInReminderWorker.WORK_NAME)
            .get()
            .single { it.state == WorkInfo.State.ENQUEUED }

        assertNotEquals(first.id, current.id)
        assertEquals(WorkInfo.State.ENQUEUED, current.state)
    }
}
