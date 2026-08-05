package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.CheckIn
import com.presencial.app.domain.model.DayInfo
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.HolidayCalculator
import com.presencial.app.domain.util.WorkdayCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class GetMonthCalendarUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(yearMonth: YearMonth): Flow<List<DayInfo>> {
        return combine(
            checkInRepository.observeCheckInsForMonth(yearMonth),
            settingsRepository.settings
        ) { checkIns, settings ->
            buildCalendar(yearMonth, checkIns, settings.countSaturdaysAsWorkdays)
        }
    }

    private fun buildCalendar(
        yearMonth: YearMonth,
        checkIns: List<CheckIn>,
        countSaturdays: Boolean
    ): List<DayInfo> {
        val today = LocalDate.now()
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

            val status = when {
                current.isAfter(today) -> DayStatus.FUTURO
                isHoliday -> DayStatus.FERIADO
                isWeekend -> DayStatus.FIM_DE_SEMANA
                savedStatus != null -> savedStatus
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
                    isEditable = !current.isAfter(today)
                )
            )
            current = current.plusDays(1)
        }
        return days
    }
}
