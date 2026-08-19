package com.presencial.app.notification

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.presencial.app.worker.GeofenceRestoreWorker
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeofenceRestoreSchedulerTest {

    private lateinit var workManager: WorkManager
    private lateinit var scheduler: GeofenceRestoreScheduler

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        workManager = WorkManager.getInstance(context)
        scheduler = GeofenceRestoreScheduler(context)
    }

    @After
    fun tearDown() {
        workManager.cancelAllWork().result.get()
    }

    @Test
    fun repeatedScheduleRestore_doesNotEnqueueDuplicateWork() {
        scheduler.scheduleRestore(ExistingWorkPolicy.KEEP)
        scheduler.scheduleRestore(ExistingWorkPolicy.KEEP)

        val workInfos = workManager
            .getWorkInfosForUniqueWork(GeofenceRestoreWorker.WORK_NAME)
            .get()

        assertEquals(1, workInfos.size)
        assertEquals(WorkInfo.State.ENQUEUED, workInfos.single().state)
    }
}
