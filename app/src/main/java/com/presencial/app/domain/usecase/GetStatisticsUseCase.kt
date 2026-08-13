package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.Absence
import com.presencial.app.domain.model.AnnualSummary
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.CheckIn
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.model.MonthlySummary
import com.presencial.app.domain.model.WeeklyAttendanceSummary
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.GoalCalculator
import com.presencial.app.domain.util.PresencePolicyCalculator
import com.presencial.app.domain.util.TimeProvider
import com.presencial.app.domain.util.WorkdayCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class StatisticsData(
    val selectedYear: Int,
    val monthlySummaries: List<MonthlySummary>,
    val averageAchieved: Float,
    val totalPresencial: Int,
    val totalHomeOffice: Int,
    val longestStreak: Int,
    val currentStreak: Int,
    val weeklySummaries: List<WeeklyAttendanceSummary>,
    val annualSummary: AnnualSummary
)

class GetStatisticsUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val absenceRepository: AbsenceRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider
) {
    operator fun invoke(selectedYear: Int = timeProvider.currentMonth().year): Flow<StatisticsData> {
        return combine(
            checkInRepository.observeAllCheckIns(),
            absenceRepository.getAllAbsences(),
            settingsRepository.settings
        ) { checkIns, absences, settings ->
            buildStatistics(checkIns, absences, settings, selectedYear)
        }
    }

    private fun buildStatistics(
        checkIns: List<CheckIn>,
        absences: List<Absence>,
        settings: AppSettings,
        selectedYear: Int
    ): StatisticsData {
        val requiredPercentage = settings.requiredPercentage
        val countSaturdays = settings.countSaturdaysAsWorkdays
        val policy = settings.presencePolicy
        val grouped = checkIns.groupBy { YearMonth.from(it.date) }
        val summaries = grouped.map { (yearMonth, monthCheckIns) ->
            buildMonthlySummary(
                yearMonth,
                monthCheckIns,
                absences,
                requiredPercentage,
                countSaturdays,
                policy
            )
        }.sortedByDescending { it.yearMonth }

        val totalPresencial = checkIns.count { it.status == DayStatus.PRESENCIAL }
        val totalHomeOffice = checkIns.count { it.status == DayStatus.HOME_OFFICE }

        val presencialDates = checkIns
            .filter { it.status == DayStatus.PRESENCIAL }
            .map { it.date }
            .sorted()

        val yearSummaries = summaries.filter { it.yearMonth.year == selectedYear }
        val annualSummary = buildAnnualSummary(selectedYear, yearSummaries)
        val weeklyMonth = weeklyMonthForYear(selectedYear, checkIns)

        return StatisticsData(
            selectedYear = selectedYear,
            monthlySummaries = summaries,
            averageAchieved = annualSummary.averageAchieved,
            totalPresencial = totalPresencial,
            totalHomeOffice = totalHomeOffice,
            longestStreak = calculateLongestStreak(presencialDates),
            currentStreak = calculateCurrentStreak(presencialDates),
            weeklySummaries = buildWeeklySummaries(checkIns, weeklyMonth),
            annualSummary = annualSummary
        )
    }

    private fun weeklyMonthForYear(selectedYear: Int, checkIns: List<CheckIn>): YearMonth {
        val current = timeProvider.currentMonth()
        if (selectedYear == current.year) {
            return current
        }
        val latestDateInYear = checkIns
            .map { it.date }
            .filter { it.year == selectedYear }
            .maxOrNull()
        return latestDateInYear?.let { YearMonth.from(it) } ?: YearMonth.of(selectedYear, 1)
    }

    private fun buildMonthlySummary(
        yearMonth: YearMonth,
        monthCheckIns: List<CheckIn>,
        absences: List<Absence>,
        requiredPercentage: Int,
        countSaturdays: Boolean,
        policy: com.presencial.app.domain.model.PresencePolicy
    ): MonthlySummary {
        val workdays = WorkdayCalculator.countLiquidWorkdaysInMonth(yearMonth, countSaturdays, absences)
        val required = PresencePolicyCalculator.calculateRequiredDays(
            yearMonth,
            countSaturdays,
            absences,
            policy
        )
        val completed = monthCheckIns.count { it.status == DayStatus.PRESENCIAL }
        val homeOffice = monthCheckIns.count { it.status == DayStatus.HOME_OFFICE }
        return MonthlySummary(
            yearMonth = yearMonth,
            workdays = workdays,
            requiredDays = required,
            completedDays = completed,
            homeOfficeDays = homeOffice,
            requiredPercentage = requiredPercentage,
            achievedPercentage = GoalCalculator.calculateAchievedPercentage(completed, required)
        )
    }

    private fun buildAnnualSummary(
        year: Int,
        yearSummaries: List<MonthlySummary>
    ): AnnualSummary {
        if (yearSummaries.isEmpty()) {
            return AnnualSummary(
                year = year,
                averageAchieved = 0f,
                totalWorkdays = 0,
                totalPresencial = 0,
                goalsMetCount = 0,
                totalMonthsWithData = 0,
                bestMonth = null,
                worstMonth = null
            )
        }
        val goalsMet = yearSummaries.count { it.completedDays >= it.requiredDays && it.requiredDays > 0 }
        val sortedByAchievement = yearSummaries.sortedByDescending { it.achievedPercentage }
        return AnnualSummary(
            year = year,
            averageAchieved = yearSummaries.map { it.achievedPercentage }.average().toFloat(),
            totalWorkdays = yearSummaries.sumOf { it.workdays },
            totalPresencial = yearSummaries.sumOf { it.completedDays },
            goalsMetCount = goalsMet,
            totalMonthsWithData = yearSummaries.size,
            bestMonth = sortedByAchievement.firstOrNull(),
            worstMonth = sortedByAchievement.lastOrNull()
        )
    }

    private fun buildWeeklySummaries(
        checkIns: List<CheckIn>,
        yearMonth: YearMonth
    ): List<WeeklyAttendanceSummary> {
        val monthCheckIns = checkIns.filter {
            YearMonth.from(it.date) == yearMonth && it.status == DayStatus.PRESENCIAL
        }
        val monthStart = yearMonth.atDay(1)
        val monthEnd = yearMonth.atEndOfMonth()
        var weekStart = monthStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val summaries = mutableListOf<WeeklyAttendanceSummary>()
        var weekIndex = 1

        while (!weekStart.isAfter(monthEnd)) {
            val weekEnd = weekStart.plusDays(WEEK_LAST_DAY_OFFSET)
            val presencialDays = monthCheckIns.count { checkIn ->
                !checkIn.date.isBefore(weekStart) &&
                    !checkIn.date.isAfter(weekEnd) &&
                    !checkIn.date.isBefore(monthStart) &&
                    !checkIn.date.isAfter(monthEnd)
            }
            summaries += WeeklyAttendanceSummary(
                weekIndex = weekIndex,
                label = "Semana $weekIndex",
                presencialDays = presencialDays
            )
            weekIndex++
            weekStart = weekStart.plusWeeks(1)
        }
        return summaries
    }

    private fun calculateLongestStreak(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        var max = 1
        var current = 1
        for (i in 1 until dates.size) {
            if (dates[i] == dates[i - 1].plusDays(1)) {
                current++
                max = maxOf(max, current)
            } else {
                current = 1
            }
        }
        return max
    }

    private fun calculateCurrentStreak(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        val sorted = dates.sortedDescending()
        var streak = 1
        for (i in 1 until sorted.size) {
            if (sorted[i] == sorted[i - 1].minusDays(1)) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    companion object {
        private const val WEEK_LAST_DAY_OFFSET = 6L
    }
}
