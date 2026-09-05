package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.Absence
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.CheckIn
import com.presencial.app.domain.model.DashboardData
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.AbsenceCoverage
import com.presencial.app.domain.util.GoalCalculator
import com.presencial.app.domain.util.PresencePolicyCalculator
import com.presencial.app.domain.util.SmartMessageMetricsCalculator
import com.presencial.app.domain.util.TimeProvider
import com.presencial.app.domain.util.WorkdayCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class GetDashboardDataUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val absenceRepository: AbsenceRepository,
    private val settingsRepository: SettingsRepository,
    private val getSmartMessageUseCase: GetSmartMessageUseCase,
    private val timeProvider: TimeProvider
) {
    operator fun invoke(yearMonth: YearMonth = timeProvider.currentMonth()): Flow<DashboardData> {
        val today = timeProvider.today()
        val yesterday = today.minusDays(1)
        val yesterdayMonth = YearMonth.from(yesterday)

        return combine(
            observeCheckInsForDashboard(yearMonth, yesterdayMonth),
            absenceRepository.getAbsencesInRange(
                minOf(yearMonth.atDay(1), yesterday),
                maxOf(yearMonth.atEndOfMonth(), yesterday)
            ),
            settingsRepository.settings
        ) { checkIns, absences, settings ->
            Triple(checkIns, absences, settings)
        }.map { (checkIns, absences, settings) ->
            val yesterdayCheckIn = (checkIns.monthCheckIns + checkIns.adjacentCheckIns)
                .find { it.date == yesterday }
            buildDashboard(
                yearMonth = yearMonth,
                checkIns = checkIns.monthCheckIns,
                absences = absences,
                settings = settings,
                yesterdayCheckIn = yesterdayCheckIn
            )
        }
    }

    private fun observeCheckInsForDashboard(
        yearMonth: YearMonth,
        yesterdayMonth: YearMonth
    ): Flow<DashboardCheckIns> {
        val monthFlow = checkInRepository.observeCheckInsForMonth(yearMonth)
        if (yesterdayMonth == yearMonth) {
            return monthFlow.map { DashboardCheckIns(monthCheckIns = it) }
        }
        return combine(
            monthFlow,
            checkInRepository.observeCheckInsForMonth(yesterdayMonth)
        ) { monthCheckIns, adjacentCheckIns ->
            DashboardCheckIns(
                monthCheckIns = monthCheckIns,
                adjacentCheckIns = adjacentCheckIns
            )
        }
    }

    private fun buildDashboard(
        yearMonth: YearMonth,
        checkIns: List<CheckIn>,
        absences: List<Absence>,
        settings: AppSettings,
        yesterdayCheckIn: CheckIn?
    ): DashboardData {
        val countSaturdays = settings.countSaturdaysAsWorkdays
        val policy = settings.presencePolicy
        val today = timeProvider.today()
        val yesterday = today.minusDays(1)
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
            smartMessage = getSmartMessageUseCase(params),
            countSaturdays = countSaturdays,
            todayIsPresencial = todayCheckIn?.status == DayStatus.PRESENCIAL,
            todayIsWorkday = WorkdayCalculator.isWorkday(today, countSaturdays),
            yesterdayIsPending = isYesterdayPending(
                yesterday = yesterday,
                yesterdayCheckIn = yesterdayCheckIn,
                absences = absences,
                countSaturdays = countSaturdays
            ),
            streak = calculateStreak(checkIns, today),
            policyCompanyName = policy.companyName
        )
    }

    private fun isYesterdayPending(
        yesterday: LocalDate,
        yesterdayCheckIn: CheckIn?,
        absences: List<Absence>,
        countSaturdays: Boolean
    ): Boolean {
        if (yesterdayCheckIn != null) return false
        if (!WorkdayCalculator.isWorkday(yesterday, countSaturdays)) return false
        return !AbsenceCoverage.coversFullDay(yesterday, absences)
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

    private data class DashboardCheckIns(
        val monthCheckIns: List<CheckIn>,
        val adjacentCheckIns: List<CheckIn> = emptyList()
    )
}
