package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.AttendanceReport
import com.presencial.app.domain.model.AttendanceReportFooter
import com.presencial.app.domain.model.AttendanceReportRow
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.GoalCalculator
import com.presencial.app.domain.util.PresencePolicyCalculator
import com.presencial.app.domain.util.TimeProvider
import com.presencial.app.domain.util.WorkdayCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.format.TextStyle
import java.util.Locale
import java.time.YearMonth
import javax.inject.Inject

class GetAttendanceReportUseCase @Inject constructor(
    private val getMonthCalendarUseCase: GetMonthCalendarUseCase,
    private val absenceRepository: AbsenceRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider
) {
    operator fun invoke(yearMonth: YearMonth): Flow<AttendanceReport> {
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()
        return combine(
            getMonthCalendarUseCase(yearMonth),
            absenceRepository.getAbsencesInRange(start, end),
            settingsRepository.settings
        ) { days, absences, settings ->
            val locale = Locale.getDefault()
            val rows = days.map { day ->
                AttendanceReportRow(
                    date = day.date,
                    dayOfWeekLabel = day.date.dayOfWeek.getDisplayName(TextStyle.FULL, locale),
                    statusLabel = day.status.toExportLabel(day.holidayName),
                    isHoliday = day.isHoliday,
                    isWorkday = day.isWorkday,
                    holidayName = day.holidayName
                )
            }
            val workdays = WorkdayCalculator.countLiquidWorkdaysInMonth(
                yearMonth,
                settings.countSaturdaysAsWorkdays,
                absences
            )
            val requiredDays = PresencePolicyCalculator.calculateRequiredDays(
                yearMonth,
                settings.countSaturdaysAsWorkdays,
                absences,
                settings.presencePolicy
            )
            val completedDays = days.count { it.status == DayStatus.PRESENCIAL }
            AttendanceReport(
                yearMonth = yearMonth,
                rows = rows,
                footer = AttendanceReportFooter(
                    workdays = workdays,
                    requiredDays = requiredDays,
                    completedDays = completedDays,
                    requiredPercentage = settings.requiredPercentage,
                    achievedPercentage = GoalCalculator.calculateAchievedPercentage(
                        completedDays,
                        requiredDays
                    ),
                    exportedAt = timeProvider.today()
                )
            )
        }
    }

    private fun DayStatus.toExportLabel(holidayName: String?): String = when (this) {
        DayStatus.PRESENCIAL -> "Presencial"
        DayStatus.HOME_OFFICE -> "Home Office"
        DayStatus.FERIADO -> holidayName?.let { "Feriado ($it)" } ?: "Feriado"
        DayStatus.FIM_DE_SEMANA -> "Fim de semana"
        DayStatus.FUTURO -> "Futuro"
        DayStatus.FALTOU -> "Faltou"
        DayStatus.ABSENCE -> "Ausência"
    }
}
