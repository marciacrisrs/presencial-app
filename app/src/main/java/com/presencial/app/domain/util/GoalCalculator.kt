package com.presencial.app.domain.util

import kotlin.math.ceil

/**
 * Calcula metas e percentuais de comparecimento presencial.
 */
object GoalCalculator {

    fun calculateRequiredDays(workdays: Int, percentage: Int): Int {
        if (workdays <= 0 || percentage <= 0) return 0
        return ceil(workdays * percentage / HUNDRED_DOUBLE).toInt()
    }

    fun calculateAchievedPercentage(completedDays: Int, requiredDays: Int): Float {
        if (requiredDays <= 0) return HUNDRED_FLOAT
        return (completedDays.toFloat() / requiredDays * HUNDRED_FLOAT).coerceAtMost(HUNDRED_FLOAT)
    }

    fun calculateProgressFraction(completedDays: Int, requiredDays: Int): Float {
        if (requiredDays <= 0) return 1f
        return (completedDays.toFloat() / requiredDays).coerceIn(0f, 1f)
    }

    fun calculateRemainingDays(completedDays: Int, requiredDays: Int): Int {
        return (requiredDays - completedDays).coerceAtLeast(0)
    }

    private const val HUNDRED_DOUBLE = 100.0
    private const val HUNDRED_FLOAT = 100f
}
