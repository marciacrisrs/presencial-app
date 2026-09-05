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
        val worker = createWorker()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 1) { syncGeofencesUseCase() }
        coVerify(exactly = 1) { widgetRefresher.refresh() }
    }

    @Test
    fun doWork_retriesTransientRegistrationFailure() = runBlocking {
        coEvery { syncGeofencesUseCase() } throws
            GeofenceRegistrationException("Play Services not ready", retryable = true)

        val result = createWorker().doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
        coVerify(exactly = 0) { widgetRefresher.refresh() }
    }

    @Test
    fun doWork_failsPermanentRegistrationFailure() = runBlocking {
        coEvery { syncGeofencesUseCase() } throws
            GeofenceRegistrationException("permission denied", retryable = false)

        val result = createWorker().doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
        coVerify(exactly = 0) { widgetRefresher.refresh() }
    }

    private fun createWorker(): GeofenceRestoreWorker {
        return TestListenableWorkerBuilder<GeofenceRestoreWorker>(context)
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
