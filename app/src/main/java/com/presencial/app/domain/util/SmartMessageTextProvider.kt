package com.presencial.app.domain.util

interface SmartMessageTextProvider {
    fun configureRequiredPercentage(): String
    fun goalMetCelebration(): String
    fun mustAttendAllRemaining(): String
    fun weeklyRequired(days: Int): String
    fun homeOfficeUntilFriday(): String
    fun closeToGoal(remaining: Int): String
    fun monthStartSuggestion(): String
    fun projectedMonthEnd(percentage: Int): String
    fun remainingDays(count: Int): String
}
