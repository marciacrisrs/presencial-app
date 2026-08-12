package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.Absence
import com.presencial.app.domain.model.AnnualSummary
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.CheckIn
import com.presencial.app.domain.model.DayInfo
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
import java.time.LocalDate
import java.time.YearMonth
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
    val annualSummary: AnnualSummary,
    val heatmapDays: List<DayInfo>
)

class GetStatisticsUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val absenceRepository: AbsenceRepository,
    private val settingsRepository: SettingsRepository,
    private val getMonthCalendarUseCase: GetMonthCalendarUseCase,
    private val timeProvider: TimeProvider
) {
    operator fun invoke(selectedYear: Int = timeProvider.currentMonth().year): Flow<StatisticsData> {
        val monthFlows = (1..MONTHS_IN_YEAR).map { month ->
            getMonthCalendarUseCase(YearMonth.of(selectedYear, month))
        }
        val yearCalendar: Flow<List<DayInfo>> = combine(monthFlows) { months ->
            months.flatMap { monthDays -> monthDays.toList() }
        }

        return combine(
            checkInRepository.observeAllCheckIns(),
            absenceRepository.getAllAbsences(),
            settingsRepository.settings,
            yearCalendar
        ) { checkIns, absences, settings, heatmapDays ->
            buildStatistics(checkIns, absences, settings, heatmapDays, selectedYear)
        }
    }

    private fun buildStatistics(
        checkIns: List<CheckIn>,
        absences: List<Absence>,
        settings: AppSettings,
        heatmapDays: List<DayInfo>,
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
        val avg = if (summaries.isNotEmpty()) {
            summaries.map { it.achievedPercentage }.average().toFloat()
        } else {
            0f
        }

        val presencialDates = checkIns
            .filter { it.status == DayStatus.PRESENCIAL }
            .map { it.date }
            .sorted()

        val yearSummaries = summaries.filter { it.yearMonth.year == selectedYear }
        val annualSummary = buildAnnualSummary(selectedYear, yearSummaries)

        return StatisticsData(
            selectedYear = selectedYear,
            monthlySummaries = summaries,
            averageAchieved = avg,
            totalPresencial = totalPresencial,
            totalHomeOffice = totalHomeOffice,
            longestStreak = calculateLongestStreak(presencialDates),
            currentStreak = calculateCurrentStreak(presencialDates),
            weeklySummaries = buildWeeklySummaries(checkIns, timeProvider.currentMonth()),
            annualSummary = annualSummary,
            heatmapDays = heatmapDays
        )
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
        val weekCount = ((yearMonth.lengthOfMonth() - 1) / DAYS_IN_WEEK) + 1
        return (1..weekCount).map { week ->
            val startDay = (week - 1) * DAYS_IN_WEEK + 1
            val endDay = minOf(week * DAYS_IN_WEEK, yearMonth.lengthOfMonth())
            val presencialDays = monthCheckIns.count { it.date.dayOfMonth in startDay..endDay }
            WeeklyAttendanceSummary(
                weekIndex = week,
                label = "Semana $week",
                presencialDays = presencialDays
            )
        }
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
        private const val MONTHS_IN_YEAR = 12
        private const val DAYS_IN_WEEK = 7
    }
}
