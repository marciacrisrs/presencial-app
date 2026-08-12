package com.presencial.app.domain.util

import com.presencial.app.domain.model.CheckIn
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.usecase.SmartMessageParams
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import kotlin.math.ceil

object SmartMessageMetricsCalculator {

    fun enrich(
        params: SmartMessageParams,
        checkIns: List<CheckIn>,
        today: LocalDate
    ): SmartMessageParams {
        val remainingWorkdays = WorkdayCalculator.countRemainingWorkdays(
            today,
            params.yearMonth,
            params.countSaturdays
        )
        val weeklyCompleted = countWeeklyPresencialDays(checkIns, today)
        val weeklyRequired = calculateWeeklyRequiredDays(params.remainingDays, remainingWorkdays)
        val projectedPercentage = calculateProjectedPercentage(
            completedDays = params.completedDays,
            requiredDays = params.requiredDays,
            remainingWorkdays = remainingWorkdays,
            remainingDays = params.remainingDays
        )

        return params.copy(
            remainingWorkdays = remainingWorkdays,
            weeklyCompletedDays = weeklyCompleted,
            weeklyRequiredDays = weeklyRequired,
            projectedMonthPercentage = projectedPercentage
        )
    }

    fun calculateWeeklyRequiredDays(remainingDays: Int, remainingWorkdays: Int): Int {
        if (remainingDays <= 0 || remainingWorkdays <= 0) return 0
        val weeksLeft = (remainingWorkdays / WORKDAYS_PER_WEEK).coerceAtLeast(MIN_WEEKS)
        return ceil(remainingDays.toDouble() / weeksLeft).toInt().coerceAtMost(remainingDays)
    }

    fun calculateProjectedPercentage(
        completedDays: Int,
        requiredDays: Int,
        remainingWorkdays: Int,
        remainingDays: Int
    ): Int {
        if (requiredDays <= 0) return FULL_PERCENTAGE
        if (remainingWorkdays <= 0) {
            return GoalCalculator.calculateAchievedPercentage(completedDays, requiredDays).toInt()
        }
        val projectedCompleted = completedDays + remainingDays.coerceAtMost(remainingWorkdays)
        return GoalCalculator.calculateAchievedPercentage(projectedCompleted, requiredDays).toInt()
    }

    private fun countWeeklyPresencialDays(checkIns: List<CheckIn>, today: LocalDate): Int {
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        return checkIns.count {
            it.status == DayStatus.PRESENCIAL &&
                !it.date.isBefore(weekStart) &&
                !it.date.isAfter(weekEnd)
        }
    }

    private const val WORKDAYS_PER_WEEK = 5.0
    private const val MIN_WEEKS = 1.0
    private const val FULL_PERCENTAGE = 100
}
