package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.CheckIn
import com.presencial.app.domain.model.DashboardData
import com.presencial.app.domain.model.DayInfo
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.model.MonthlySummary
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.MonthlySummaryRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.GoalCalculator
import com.presencial.app.domain.util.HolidayCalculator
import com.presencial.app.domain.util.SmartMessageGenerator
import com.presencial.app.domain.util.WorkdayCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class GetDashboardDataUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(yearMonth: YearMonth = YearMonth.now()): Flow<DashboardData> {
        return combine(
            checkInRepository.observeCheckInsForMonth(yearMonth),
            settingsRepository.settings
        ) { checkIns, settings ->
            buildDashboard(yearMonth, checkIns, settings.requiredPercentage, settings.countSaturdaysAsWorkdays)
        }
    }

    private fun buildDashboard(
        yearMonth: YearMonth,
        checkIns: List<CheckIn>,
        requiredPercentage: Int,
        countSaturdays: Boolean
    ): DashboardData {
        val today = LocalDate.now()
        val workdays = WorkdayCalculator.countWorkdaysInMonth(yearMonth, countSaturdays)
        val requiredDays = GoalCalculator.calculateRequiredDays(workdays, requiredPercentage)
        val completedDays = checkIns.count { it.status == DayStatus.PRESENCIAL }
        val homeOfficeDays = checkIns.count { it.status == DayStatus.HOME_OFFICE }
        val remainingDays = GoalCalculator.calculateRemainingDays(completedDays, requiredDays)
        val achievedPercentage = GoalCalculator.calculateAchievedPercentage(completedDays, requiredDays)
        val progressFraction = GoalCalculator.calculateProgressFraction(completedDays, requiredDays)
        val todayCheckIn = checkIns.find { it.date == today }
        val yesterday = today.minusDays(1)
        val yesterdayCheckIn = checkIns.find { it.date == yesterday }
        val yesterdayIsPending = WorkdayCalculator.isWorkday(yesterday, countSaturdays) && 
                yesterdayCheckIn == null
        
        val streak = calculateStreak(checkIns, today)

        return DashboardData(
            yearMonth = yearMonth,
            totalDays = yearMonth.lengthOfMonth(),
            workdays = workdays,
            requiredDays = requiredDays,
            completedDays = completedDays,
            remainingDays = remainingDays,
            homeOfficeDays = homeOfficeDays,
            achievedPercentage = achievedPercentage,
            requiredPercentage = requiredPercentage,
            progressFraction = progressFraction,
            smartMessage = SmartMessageGenerator.generate(
                completedDays, requiredDays, remainingDays, achievedPercentage,
                today, yearMonth, countSaturdays
            ),
            todayIsPresencial = todayCheckIn?.status == DayStatus.PRESENCIAL,
            todayIsWorkday = WorkdayCalculator.isWorkday(today, countSaturdays),
            yesterdayIsPending = yesterdayIsPending,
            streak = streak
        )
    }

    private fun calculateStreak(checkIns: List<CheckIn>, today: LocalDate): Int {
        val presencialDates = checkIns
            .filter { it.status == DayStatus.PRESENCIAL }
            .map { it.date }
            .toSet()
        var streak = 0
        var date = today
        while (presencialDates.contains(date)) {
            streak++
            date = date.minusDays(1)
        }
        return streak
    }
}
