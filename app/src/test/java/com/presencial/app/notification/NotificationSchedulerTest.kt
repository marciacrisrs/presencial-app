package com.presencial.app.notification

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkInfo
import com.presencial.app.worker.CheckInReminderWorker
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NotificationSchedulerTest {

    private lateinit var context: Context
    private lateinit var scheduler: NotificationScheduler

    @BeforeEach
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        scheduler = NotificationScheduler(context)
    }

    @AfterEach
    fun tearDown() {
        androidx.work.WorkManager.getInstance(context).cancelAllWork().result.get()
    }

    @Test
    fun `rescheduling reminder replaces previous periodic work`() {
        val workManager = androidx.work.WorkManager.getInstance(context)

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
        val firstState = workManager.getWorkInfoById(first.id).get()?.state
        assertEquals(WorkInfo.State.CANCELLED, firstState)
    }
}
