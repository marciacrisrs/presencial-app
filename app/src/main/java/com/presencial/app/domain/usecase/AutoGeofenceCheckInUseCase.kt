package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.Absence
import com.presencial.app.domain.model.CheckInSource
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.MonthlySummaryRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.TimeProvider
import com.presencial.app.domain.util.WorkdayCalculator
import com.presencial.app.domain.widget.WidgetRefresher
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

sealed class AutoCheckInResult {
    data object Success : AutoCheckInResult()
    data object SkippedAlreadyCheckedIn : AutoCheckInResult()
    data object SkippedNonWorkday : AutoCheckInResult()
    data object SkippedAbsence : AutoCheckInResult()
}

class AutoGeofenceCheckInUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val monthlySummaryRepository: MonthlySummaryRepository,
    private val settingsRepository: SettingsRepository,
    private val absenceRepository: AbsenceRepository,
    private val timeProvider: TimeProvider,
    private val widgetRefresher: WidgetRefresher
) {
    suspend operator fun invoke(workAddressId: Long?): AutoCheckInResult {
        val today = timeProvider.today()
        val settings = settingsRepository.settings.first()

        if (!WorkdayCalculator.isWorkday(today, settings.countSaturdaysAsWorkdays)) {
            return AutoCheckInResult.SkippedNonWorkday
        }

        val absences = absenceRepository.getAbsencesInRange(today, today).first()
        if (hasFullDayAbsence(today, absences)) {
            return AutoCheckInResult.SkippedAbsence
        }

        val existing = checkInRepository.getCheckIn(today)
        if (existing != null) {
            return AutoCheckInResult.SkippedAlreadyCheckedIn
        }

        checkInRepository.saveCheckIn(
            date = today,
            status = DayStatus.PRESENCIAL,
            source = CheckInSource.AUTO_GEOFENCE,
            workAddressId = workAddressId
        )
        monthlySummaryRepository.refreshSummary(YearMonth.from(today))
        widgetRefresher.refresh()
        return AutoCheckInResult.Success
    }

    private fun hasFullDayAbsence(date: LocalDate, absences: List<Absence>): Boolean =
        absences.any { absence ->
            !date.isBefore(absence.startDate) &&
                !date.isAfter(absence.endDate) &&
                absence.isFullDay
        }
}
