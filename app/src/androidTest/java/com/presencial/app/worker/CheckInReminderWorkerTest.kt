package com.presencial.app.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.CheckIn
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.WorkdayCalculator
import com.presencial.app.domain.widget.WidgetRefresher
import com.presencial.app.notification.NotificationHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class CheckInReminderWorkerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val checkInRepository: CheckInRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val notificationHelper: NotificationHelper = mockk(relaxed = true)
    private val widgetRefresher: WidgetRefresher = mockk(relaxed = true)

    @Before
    fun setup() {
        mockkObject(WorkdayCalculator)
    }

    @Test
    fun doWork_onWorkday_noCheckIn_showsNotification() = runBlocking {
        val today = LocalDate.now()
        coEvery { settingsRepository.settings } returns flowOf(AppSettings(countSaturdaysAsWorkdays = false))
        every { WorkdayCalculator.isWorkday(today, false) } returns true
        coEvery { checkInRepository.getCheckIn(today) } returns null

        val worker = TestListenableWorkerBuilder<CheckInReminderWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters
                ): ListenableWorker {
                    return CheckInReminderWorker(
                        appContext,
                        workerParameters,
                        checkInRepository,
                        settingsRepository,
                        notificationHelper,
                        widgetRefresher
                    )
                }
            })
            .build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify { widgetRefresher.refresh() }
        verify { notificationHelper.showCheckInReminder() }
    }
}
