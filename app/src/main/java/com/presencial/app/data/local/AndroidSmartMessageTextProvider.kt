package com.presencial.app.data.local

import android.content.Context
import com.presencial.app.R
import com.presencial.app.domain.util.SmartMessageTextProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidSmartMessageTextProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : SmartMessageTextProvider {

    override fun configureRequiredPercentage(): String =
        context.getString(R.string.smart_msg_configure_percentage)

    override fun goalMetCelebration(): String =
        context.getString(R.string.smart_msg_goal_met)

    override fun mustAttendAllRemaining(): String =
        context.getString(R.string.smart_msg_must_attend_all)

    override fun weeklyRequired(days: Int): String =
        context.getString(R.string.smart_msg_weekly_required, days)

    override fun homeOfficeUntilFriday(): String =
        context.getString(R.string.smart_msg_home_office_until_friday)

    override fun closeToGoal(remaining: Int): String =
        context.getString(R.string.smart_msg_close_to_goal, remaining)

    override fun monthStartSuggestion(): String =
        context.getString(R.string.smart_msg_month_start)

    override fun projectedMonthEnd(percentage: Int): String =
        context.getString(R.string.smart_msg_projected_end, percentage)

    override fun remainingDays(count: Int): String =
        context.resources.getQuantityString(R.plurals.smart_msg_remaining_days, count, count)

    override fun fallbackGoalCompleted(): String =
        context.getString(R.string.smart_msg_fallback_goal_completed)

    override fun fallbackRemainingDays(count: Int): String =
        context.resources.getQuantityString(R.plurals.smart_msg_fallback_remaining, count, count)
}
