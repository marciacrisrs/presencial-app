package com.presencial.app.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.presencial.app.domain.location.GeofenceRegistrationException
import com.presencial.app.domain.usecase.SyncGeofencesUseCase
import com.presencial.app.domain.widget.WidgetRefresher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeofenceRestoreWorkerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val syncGeofencesUseCase: SyncGeofencesUseCase = mockk(relaxed = true)
    private val widgetRefresher: WidgetRefresher = mockk(relaxed = true)

    @Test
    fun doWork_syncsGeofencesAndRefreshesWidget() = runBlocking {
        val result = restoreWorker().doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 1) { syncGeofencesUseCase() }
        coVerify(exactly = 1) { widgetRefresher.refresh() }
    }

    @Test
    fun doWork_retriesTransientSyncFailure() = runBlocking {
        coEvery { syncGeofencesUseCase() } throws
            GeofenceRegistrationException("geofence not available", retryable = true)

        val worker = restoreWorker(runAttemptCount = 0)

        assertEquals(ListenableWorker.Result.retry(), worker.doWork())
        coVerify(exactly = 0) { widgetRefresher.refresh() }
    }

    @Test
    fun doWork_doesNotRetryNonRetryableFailure() = runBlocking {
        coEvery { syncGeofencesUseCase() } throws
            GeofenceRegistrationException("permission denied", retryable = false)

        val worker = restoreWorker(runAttemptCount = 0)

        assertEquals(ListenableWorker.Result.failure(), worker.doWork())
        coVerify(exactly = 0) { widgetRefresher.refresh() }
    }

    @Test
    fun doWork_failsAfterMaxRestoreAttempts() = runBlocking {
        coEvery { syncGeofencesUseCase() } throws
            GeofenceRegistrationException("geofence not available", retryable = true)

        val worker = restoreWorker(runAttemptCount = GeofenceRestoreEvents.MAX_RESTORE_ATTEMPTS - 1)

        assertEquals(ListenableWorker.Result.failure(), worker.doWork())
        coVerify(exactly = 0) { widgetRefresher.refresh() }
    }

    private fun restoreWorker(runAttemptCount: Int = 0): GeofenceRestoreWorker {
        return TestListenableWorkerBuilder<GeofenceRestoreWorker>(context)
            .setRunAttemptCount(runAttemptCount)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker {
                    return GeofenceRestoreWorker(
                        appContext,
                        workerParameters,
                        syncGeofencesUseCase,
                        widgetRefresher
                    )
                }
            })
            .build()
    }
}
