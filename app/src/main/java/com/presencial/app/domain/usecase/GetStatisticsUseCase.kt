package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.CheckIn
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.model.MonthlySummary
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.GoalCalculator
import com.presencial.app.domain.util.WorkdayCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.YearMonth
import javax.inject.Inject

data class StatisticsData(
    val monthlySummaries: List<MonthlySummary>,
    val averageAchieved: Float,
    val totalPresencial: Int,
    val totalHomeOffice: Int,
    val longestStreak: Int,
    val currentStreak: Int
)

class GetStatisticsUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<StatisticsData> {
        return combine(
            checkInRepository.observeAllCheckIns(),
            settingsRepository.settings
        ) { checkIns, settings ->
            buildStatistics(checkIns, settings.requiredPercentage, settings.countSaturdaysAsWorkdays)
        }
    }

    private fun buildStatistics(
        checkIns: List<CheckIn>,
        requiredPercentage: Int,
        countSaturdays: Boolean
    ): StatisticsData {
        val grouped = checkIns.groupBy { YearMonth.from(it.date) }
        val summaries = grouped.map { (yearMonth, monthCheckIns) ->
            val workdays = WorkdayCalculator.countWorkdaysInMonth(yearMonth, countSaturdays)
            val required = GoalCalculator.calculateRequiredDays(workdays, requiredPercentage)
            val completed = monthCheckIns.count { it.status == DayStatus.PRESENCIAL }
            val homeOffice = monthCheckIns.count { it.status == DayStatus.HOME_OFFICE }
            MonthlySummary(
                yearMonth = yearMonth,
                workdays = workdays,
                requiredDays = required,
                completedDays = completed,
                homeOfficeDays = homeOffice,
                requiredPercentage = requiredPercentage,
                achievedPercentage = GoalCalculator.calculateAchievedPercentage(completed, required)
            )
        }.sortedByDescending { it.yearMonth }

        val totalPresencial = checkIns.count { it.status == DayStatus.PRESENCIAL }
        val totalHomeOffice = checkIns.count { it.status == DayStatus.HOME_OFFICE }
        val avg = if (summaries.isNotEmpty()) {
            summaries.map { it.achievedPercentage }.average().toFloat()
        } else 0f

        val presencialDates = checkIns
            .filter { it.status == DayStatus.PRESENCIAL }
            .map { it.date }
            .sorted()

        return StatisticsData(
            monthlySummaries = summaries,
            averageAchieved = avg,
            totalPresencial = totalPresencial,
            totalHomeOffice = totalHomeOffice,
            longestStreak = calculateLongestStreak(presencialDates),
            currentStreak = calculateCurrentStreak(presencialDates)
        )
    }

    private fun calculateLongestStreak(dates: List<java.time.LocalDate>): Int {
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

    private fun calculateCurrentStreak(dates: List<java.time.LocalDate>): Int {
        if (dates.isEmpty()) return 0
        val sorted = dates.sortedDescending()
        var streak = 1
        for (i in 1 until sorted.size) {
            if (sorted[i] == sorted[i - 1].minusDays(1)) streak++ else break
        }
        return streak
    }
}
