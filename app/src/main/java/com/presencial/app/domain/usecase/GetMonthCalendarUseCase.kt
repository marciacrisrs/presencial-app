package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.Absence
import com.presencial.app.domain.model.CheckIn
import com.presencial.app.domain.model.DayInfo
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.HolidayCalculator
import com.presencial.app.domain.util.TimeProvider
import com.presencial.app.domain.util.WorkdayCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class GetMonthCalendarUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val absenceRepository: AbsenceRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider
) {
    operator fun invoke(yearMonth: YearMonth): Flow<List<DayInfo>> {
        return combine(
            checkInRepository.observeCheckInsForMonth(yearMonth),
            absenceRepository.getAbsencesInRange(yearMonth.atDay(1), yearMonth.atEndOfMonth()),
            settingsRepository.settings
        ) { checkIns, absences, settings ->
            buildCalendar(yearMonth, checkIns, absences, settings.countSaturdaysAsWorkdays)
        }
    }

    private fun buildCalendar(
        yearMonth: YearMonth,
        checkIns: List<CheckIn>,
        absences: List<Absence>,
        countSaturdays: Boolean
    ): List<DayInfo> {
        val today = timeProvider.today()
        val checkInMap = checkIns.associateBy { it.date }
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()
        val days = mutableListOf<DayInfo>()
        var current = start

        while (!current.isAfter(end)) {
            val isHoliday = HolidayCalculator.isHoliday(current)
            val holiday = HolidayCalculator.getHoliday(current)
            val isWeekend = WorkdayCalculator.isWeekend(current, countSaturdays)
            val isWorkday = WorkdayCalculator.isWorkday(current, countSaturdays)
            val savedStatus = checkInMap[current]?.status
            val savedSource = checkInMap[current]?.source ?: "MANUAL"
            val isAbsent = absences.any { absence ->
                !current.isBefore(absence.startDate) && !current.isAfter(absence.endDate) && absence.isFullDay
            }

            val status = when {
                isAbsent -> DayStatus.ABSENCE
                savedStatus != null -> savedStatus
                current.isAfter(today) -> DayStatus.FUTURO
                isHoliday -> DayStatus.FERIADO
                isWeekend -> DayStatus.FIM_DE_SEMANA
                isWorkday && current.isBefore(today) -> DayStatus.FALTOU
                else -> DayStatus.HOME_OFFICE
            }

            days.add(
                DayInfo(
                    date = current,
                    status = status,
                    isWorkday = isWorkday,
                    isHoliday = isHoliday,
                    holidayName = holiday?.name,
                    isEditable = !current.isAfter(today) || isAbsent,
                    source = savedSource
                )
            )
            current = current.plusDays(1)
        }
        return days
    }
}
