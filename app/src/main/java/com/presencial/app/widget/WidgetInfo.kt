package com.presencial.app.widget

import com.presencial.app.domain.util.GoalCalculator
import java.time.YearMonth
import java.util.Locale

data class WidgetInfo(
    val completed: Int,
    val required: Int,
    val remaining: Int,
    val progressFraction: Float,
    val monthName: String,
    val achievedPercentage: Int,
    val status: WidgetStatus,
    val todayIsPresencial: Boolean,
    val todayIsWorkday: Boolean
) {
    companion object {
        fun create(
            completed: Int,
            required: Int,
            remaining: Int,
            remainingWorkdays: Int,
            achievedPercentage: Int,
            todayIsPresencial: Boolean,
            todayIsWorkday: Boolean,
            yearMonth: YearMonth,
            locale: Locale = Locale.getDefault()
        ): WidgetInfo {
            val progressFraction = GoalCalculator.calculateProgressFraction(completed, required)
            val monthName = yearMonth.month.getDisplayName(
                java.time.format.TextStyle.FULL,
                locale
            ).uppercase(locale)

            return WidgetInfo(
                completed = completed,
                required = required,
                remaining = remaining,
                progressFraction = progressFraction,
                monthName = monthName,
                achievedPercentage = achievedPercentage,
                status = resolveStatus(required, remaining, remainingWorkdays),
                todayIsPresencial = todayIsPresencial,
                todayIsWorkday = todayIsWorkday
            )
        }

        fun resolveStatus(
            required: Int,
            remaining: Int,
            remainingWorkdays: Int
        ): WidgetStatus = when {
            required <= 0 -> WidgetStatus.NO_GOAL
            remaining <= 0 -> WidgetStatus.GOAL_MET
            remaining > remainingWorkdays && remainingWorkdays > 0 -> WidgetStatus.BEHIND
            else -> WidgetStatus.ON_TRACK
        }
    }
}
