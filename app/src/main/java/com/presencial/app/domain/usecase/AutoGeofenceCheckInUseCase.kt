package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.CheckInSource
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.MonthlySummaryRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.TimeProvider
import com.presencial.app.domain.util.WorkdayCalculator
import kotlinx.coroutines.flow.first
import java.time.YearMonth
import javax.inject.Inject

sealed class AutoCheckInResult {
    data object Success : AutoCheckInResult()
    data object SkippedAlreadyCheckedIn : AutoCheckInResult()
    data object SkippedNonWorkday : AutoCheckInResult()
}

class AutoGeofenceCheckInUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val monthlySummaryRepository: MonthlySummaryRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider
) {
    suspend operator fun invoke(workAddressId: Long?): AutoCheckInResult {
        val today = timeProvider.today()
        val settings = settingsRepository.settings.first()

        if (!WorkdayCalculator.isWorkday(today, settings.countSaturdaysAsWorkdays)) {
            return AutoCheckInResult.SkippedNonWorkday
        }

        val existing = checkInRepository.getCheckIn(today)
        if (existing?.status == DayStatus.PRESENCIAL) {
            return AutoCheckInResult.SkippedAlreadyCheckedIn
        }

        checkInRepository.saveCheckIn(
            date = today,
            status = DayStatus.PRESENCIAL,
            source = CheckInSource.AUTO_GEOFENCE,
            workAddressId = workAddressId
        )
        monthlySummaryRepository.refreshSummary(YearMonth.from(today))
        return AutoCheckInResult.Success
    }
}
