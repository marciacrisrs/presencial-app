package com.presencial.app.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.presencial.app.domain.widget.WidgetRefresher
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetRefreshWorkerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val widgetRefresher: WidgetRefresher = mockk(relaxed = true)

    @Test
    fun doWork_refreshesWidget() = runBlocking {
        val worker = TestListenableWorkerBuilder<WidgetRefreshWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters
                ): ListenableWorker {
                    return WidgetRefreshWorker(
                        appContext,
                        workerParameters,
                        widgetRefresher
                    )
                }
            })
            .build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify { widgetRefresher.refresh() }
    }
}
