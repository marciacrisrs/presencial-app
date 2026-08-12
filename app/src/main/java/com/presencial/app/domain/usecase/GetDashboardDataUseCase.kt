package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.Absence
import com.presencial.app.domain.model.CheckIn
import com.presencial.app.domain.model.DashboardData
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.util.GoalCalculator
import com.presencial.app.domain.util.PresencePolicyCalculator
import com.presencial.app.domain.util.SmartMessageFallback
import com.presencial.app.domain.util.SmartMessageMetricsCalculator
import com.presencial.app.domain.util.TimeProvider
import com.presencial.app.domain.util.WorkdayCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transform
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class GetDashboardDataUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val absenceRepository: AbsenceRepository,
    private val settingsRepository: SettingsRepository,
    private val getAiSmartMessageUseCase: GetAiSmartMessageUseCase,
    private val smartMessageFallback: SmartMessageFallback,
    private val timeProvider: TimeProvider
) {
    operator fun invoke(yearMonth: YearMonth = timeProvider.currentMonth()): Flow<DashboardData> {
        return combine(
            checkInRepository.observeCheckInsForMonth(yearMonth),
            absenceRepository.getAbsencesInRange(yearMonth.atDay(1), yearMonth.atEndOfMonth()),
            settingsRepository.settings
        ) { checkIns, absences, settings ->
            Triple(checkIns, absences, settings)
        }.transform { (checkIns, absences, settings) ->
            val dashboard = buildDashboard(
                yearMonth,
                checkIns,
                absences,
                settings
            )
            emit(dashboard.copy(isLoadingAi = true))
            val baseParams = SmartMessageParams(
                completedDays = dashboard.completedDays,
                requiredDays = dashboard.requiredDays,
                remainingDays = dashboard.remainingDays,
                achievedPercentage = dashboard.achievedPercentage,
                today = timeProvider.today(),
                yearMonth = dashboard.yearMonth,
                countSaturdays = dashboard.countSaturdays
            )
            val enrichedParams = SmartMessageMetricsCalculator.enrich(
                params = baseParams,
                checkIns = checkIns,
                today = timeProvider.today()
            )
            val aiMessage = getAiSmartMessageUseCase(enrichedParams)
            emit(dashboard.copy(smartMessage = aiMessage, isLoadingAi = false))
        }
    }

    private fun buildDashboard(
        yearMonth: YearMonth,
        checkIns: List<CheckIn>,
        absences: List<Absence>,
        settings: AppSettings
    ): DashboardData {
        val countSaturdays = settings.countSaturdaysAsWorkdays
        val policy = settings.presencePolicy
        val today = timeProvider.today()
        val workdays = WorkdayCalculator.countLiquidWorkdaysInMonth(yearMonth, countSaturdays, absences)
        val requiredDays = PresencePolicyCalculator.calculateRequiredDays(
            yearMonth,
            countSaturdays,
            absences,
            policy
        )
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

        val params = SmartMessageMetricsCalculator.enrich(
            params = SmartMessageParams(
                completedDays = completedDays,
                requiredDays = requiredDays,
                remainingDays = remainingDays,
                achievedPercentage = achievedPercentage,
                today = today,
                yearMonth = yearMonth,
                countSaturdays = countSaturdays
            ),
            checkIns = checkIns,
            today = today
        )

        return DashboardData(
            yearMonth = yearMonth,
            totalDays = yearMonth.lengthOfMonth(),
            workdays = workdays,
            requiredDays = requiredDays,
            completedDays = completedDays,
            remainingDays = remainingDays,
            homeOfficeDays = homeOfficeDays,
            achievedPercentage = achievedPercentage,
            requiredPercentage = settings.requiredPercentage,
            progressFraction = progressFraction,
            smartMessage = smartMessageFallback.generate(params),
            countSaturdays = countSaturdays,
            todayIsPresencial = todayCheckIn?.status == DayStatus.PRESENCIAL,
            todayIsWorkday = WorkdayCalculator.isWorkday(today, countSaturdays),
            yesterdayIsPending = yesterdayIsPending,
            streak = streak,
            policyCompanyName = policy.companyName
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
