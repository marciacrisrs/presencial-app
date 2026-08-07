package com.presencial.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.util.WorkdayCalculator
import com.presencial.app.notification.NotificationHelper
import com.presencial.app.domain.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class CheckInReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val checkInRepository: CheckInRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val settings = settingsRepository.settings.first()

        val isWorkday = WorkdayCalculator.isWorkday(today, settings.countSaturdaysAsWorkdays)
        val existing = checkInRepository.getCheckIn(today)
        val isAlreadyCheckedIn = existing?.status == DayStatus.PRESENCIAL

        if (isWorkday && !isAlreadyCheckedIn) {
            notificationHelper.showCheckInReminder()
        }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "check_in_reminder"
    }
}
