package com.presencial.app.widget

import android.content.Context
import com.presencial.app.data.local.PresencialDatabase
import com.presencial.app.data.local.mapper.toDomain
import com.presencial.app.data.preferences.PresencePolicyMapper
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.util.AbsenceCoverage
import com.presencial.app.domain.util.GoalCalculator
import com.presencial.app.domain.util.PresencePolicyCalculator
import com.presencial.app.domain.util.WorkdayCalculator
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth

object WidgetInfoLoader {

    suspend fun load(context: Context): WidgetInfo {
        val db = PresencialDatabase.getInstance(context)
        val prefs = context.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE)
        val percentage = prefs.getInt(PREF_REQUIRED_PERCENTAGE, DEFAULT_REQUIRED_PERCENTAGE)
        val countSaturdays = prefs.getBoolean(PREF_COUNT_SATURDAYS, false)
        val policy = PresencePolicyMapper.fromJson(
            prefs.getString(PREF_PRESENCE_POLICY, null),
            percentage
        )

        val today = LocalDate.now()
        val yearMonth = YearMonth.from(today)
        val start = yearMonth.atDay(1).toEpochDay()
        val end = yearMonth.atEndOfMonth().toEpochDay()

        val absences = db.absenceDao()
            .getAbsencesInRange(start, end)
            .first()
            .map { it.toDomain() }

        val required = PresencePolicyCalculator.calculateRequiredDays(
            yearMonth,
            countSaturdays,
            absences,
            policy
        )

        val checkIns = db.checkInDao().getBetween(start, end).map { it.toDomain() }
        val completed = checkIns.count { AbsenceCoverage.isPresencialWorkday(it, absences) }
        val remaining = GoalCalculator.calculateRemainingDays(completed, required)
        val remainingWorkdays = WorkdayCalculator.countRemainingWorkdays(
            today,
            yearMonth,
            countSaturdays
        )
        val achievedPercentage = GoalCalculator
            .calculateAchievedPercentage(completed, required)
            .toInt()

        val todayCheckIn = checkIns.find { it.date == today }
        val todayStatus = when {
            AbsenceCoverage.coversFullDay(today, absences) -> WidgetTodayStatus.PENDING
            todayCheckIn?.status == DayStatus.PRESENCIAL -> WidgetTodayStatus.PRESENCIAL
            todayCheckIn?.status == DayStatus.HOME_OFFICE -> WidgetTodayStatus.HOME_OFFICE
            else -> WidgetTodayStatus.PENDING
        }
        val todayIsWorkday = WorkdayCalculator.isWorkday(today, countSaturdays) &&
            !AbsenceCoverage.coversFullDay(today, absences)

        return WidgetInfo.create(
            completed = completed,
            required = required,
            remaining = remaining,
            remainingWorkdays = remainingWorkdays,
            achievedPercentage = achievedPercentage,
            todayStatus = todayStatus,
            todayIsWorkday = todayIsWorkday,
            yearMonth = yearMonth
        )
    }

    private const val WIDGET_PREFS = "presencial_settings"
    private const val PREF_REQUIRED_PERCENTAGE = "required_percentage"
    private const val PREF_COUNT_SATURDAYS = "count_saturdays_as_workdays"
    private const val PREF_PRESENCE_POLICY = "presence_policy_json"
    private const val DEFAULT_REQUIRED_PERCENTAGE = 40
}
