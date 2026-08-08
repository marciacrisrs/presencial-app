package com.presencial.app.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.CheckIn
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.WorkdayCalculator
import com.presencial.app.notification.NotificationHelper
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CheckInReminderWorkerTest {

    private lateinit var context: Context
    private val checkInRepository: CheckInRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val notificationHelper: NotificationHelper = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockkObject(WorkdayCalculator)
    }

    @Test
    fun doWork_onWorkday_noCheckIn_showsNotification() = runBlocking {
        // Arrange
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
                ): ListenableWorker? {
                    return CheckInReminderWorker(
                        appContext,
                        workerParameters,
                        checkInRepository,
                        settingsRepository,
                        notificationHelper
                    )
                }
            })
            .build()

        // Act
        val result = worker.doWork()

        // Assert
        assertEquals(ListenableWorker.Result.success(), result)
        verify { notificationHelper.showCheckInReminder() }
    }

    @Test
    fun doWork_alreadyCheckedIn_doesNotShowNotification() = runBlocking {
        // Arrange
        val today = LocalDate.now()
        coEvery { settingsRepository.settings } returns flowOf(AppSettings(countSaturdaysAsWorkdays = false))
        every { WorkdayCalculator.isWorkday(today, false) } returns true
        coEvery { checkInRepository.getCheckIn(today) } returns CheckIn(today, DayStatus.PRESENCIAL)

        val worker = TestListenableWorkerBuilder<CheckInReminderWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters
                ): ListenableWorker? {
                    return CheckInReminderWorker(
                        appContext,
                        workerParameters,
                        checkInRepository,
                        settingsRepository,
                        notificationHelper
                    )
                }
            })
            .build()

        // Act
        val result = worker.doWork()

        // Assert
        assertEquals(ListenableWorker.Result.success(), result)
        verify(exactly = 0) { notificationHelper.showCheckInReminder() }
    }
}
