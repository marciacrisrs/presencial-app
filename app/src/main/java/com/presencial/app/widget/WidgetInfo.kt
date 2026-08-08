package com.presencial.app.widget

import com.presencial.app.domain.util.GoalCalculator
import java.time.YearMonth
import java.util.Locale

data class WidgetInfo(
    val completed: Int,
    val required: Int,
    val remaining: Int,
    val progressFraction: Float,
    val monthName: String
) {
    companion object {
        fun create(
            completed: Int,
            required: Int,
            remaining: Int,
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
                monthName = monthName
            )
        }
    }
}
