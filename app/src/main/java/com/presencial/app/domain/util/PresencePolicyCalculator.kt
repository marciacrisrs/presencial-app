package com.presencial.app.domain.util

import com.presencial.app.domain.model.Absence
import com.presencial.app.domain.model.PolicyConflictPriority
import com.presencial.app.domain.model.PolicyValidationResult
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.model.WeekParity
import com.presencial.app.domain.model.WeeklyPolicySummary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.ceil

object PresencePolicyCalculator {

    fun validate(policy: PresencePolicy): PolicyValidationResult {
        val normalized = policy.normalized()
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (normalized.fixedWeekdaysEnabled && normalized.mandatoryWeekdays.isEmpty()) {
            warnings += "Selecione ao menos um dia fixo presencial."
        }

        if (normalized.alternatingWeeksEnabled && normalized.fixedWeekdaysEnabled) {
            warnings += "Dias fixos valem em todas as semanas, inclusive nas remotas."
        }

        if (normalized.freePercentageEnabled && normalized.fixedWeekdaysEnabled &&
            normalized.conflictPriority == PolicyConflictPriority.UNION_MAX
        ) {
            warnings += "Com prioridade \"maior valor\", o percentual livre pode elevar a meta além dos dias fixos."
        }

        return PolicyValidationResult(
            isValid = errors.isEmpty(),
            warnings = warnings,
            errors = errors
        )
    }

    fun calculateRequiredDays(
        yearMonth: YearMonth,
        countSaturdays: Boolean,
        absences: List<Absence>,
        policy: PresencePolicy
    ): Int {
        val normalized = policy.normalized()
        val liquidWorkdays = WorkdayCalculator.countLiquidWorkdaysInMonth(
            yearMonth,
            countSaturdays,
            absences
        )
        val ruleDates = collectPolicyRequiredDates(
            yearMonth,
            countSaturdays,
            absences,
            normalized
        )
        val percentageDays = GoalCalculator.calculateRequiredDays(
            liquidWorkdays,
            normalized.freePercentage
        )

        return when (normalized.conflictPriority) {
            PolicyConflictPriority.UNION_MAX ->
                maxOf(percentageDays, ruleDates.size)
            PolicyConflictPriority.FIXED_FIRST ->
                maxOf(ruleDates.size, percentageDays)
        }
    }

    fun isPolicyRequired(
        date: LocalDate,
        countSaturdays: Boolean,
        absences: List<Absence>,
        policy: PresencePolicy
    ): Boolean {
        if (isAbsent(date, absences)) return false
        if (!WorkdayCalculator.isWorkday(date, countSaturdays)) return false

        val normalized = policy.normalized()
        if (normalized.fixedWeekdaysEnabled && date.dayOfWeek in normalized.mandatoryWeekdays) {
            return true
        }
        if (normalized.alternatingWeeksEnabled && isOnSiteWeek(date, normalized)) {
            return true
        }
        return false
    }

    fun collectPolicyRequiredDates(
        yearMonth: YearMonth,
        countSaturdays: Boolean,
        absences: List<Absence>,
        policy: PresencePolicy
    ): Set<LocalDate> {
        val normalized = policy.normalized()
        val dates = mutableSetOf<LocalDate>()
        var current = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()

        while (!current.isAfter(end)) {
            if (isPolicyRequired(current, countSaturdays, absences, normalized)) {
                dates += current
            }
            current = current.plusDays(1)
        }
        return dates
    }

    fun buildWeeklySummaries(
        yearMonth: YearMonth,
        countSaturdays: Boolean,
        absences: List<Absence>,
        policy: PresencePolicy
    ): List<WeeklyPolicySummary> {
        val normalized = policy.normalized()
        val monthStart = yearMonth.atDay(1)
        val monthEnd = yearMonth.atEndOfMonth()
        var weekStart = monthStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        val summaries = mutableListOf<WeeklyPolicySummary>()
        while (!weekStart.isAfter(monthEnd)) {
            val weekEnd = weekStart.plusDays(WEEK_LAST_DAY_OFFSET)
            val requiredDates = (0 until DAYS_IN_WEEK).map { weekStart.plusDays(it.toLong()) }
                .filter { date ->
                    !date.isBefore(monthStart) &&
                        !date.isAfter(monthEnd) &&
                        isPolicyRequired(date, countSaturdays, absences, normalized)
                }

            summaries += WeeklyPolicySummary(
                weekStart = weekStart,
                weekEnd = weekEnd,
                isOnSiteWeek = isOnSiteWeek(weekStart.plusDays(MID_WEEK_OFFSET), normalized),
                requiredDates = requiredDates,
                requiredCount = requiredDates.size
            )
            weekStart = weekStart.plusWeeks(1)
        }
        return summaries
    }

    fun isOnSiteWeek(date: LocalDate, policy: PresencePolicy): Boolean {
        if (!policy.alternatingWeeksEnabled) return false
        val anchorMonday = policy.alternatingAnchorDate
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val dateMonday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weeksBetween = ChronoUnit.WEEKS.between(anchorMonday, dateMonday)
        val onSiteByAnchor = weeksBetween % 2 == 0L
        return when (policy.onSiteWeekParity) {
            WeekParity.EVEN -> onSiteByAnchor
            WeekParity.ODD -> !onSiteByAnchor
        }
    }

    private fun isAbsent(date: LocalDate, absences: List<Absence>): Boolean =
        absences.any { absence ->
            !date.isBefore(absence.startDate) &&
                !date.isAfter(absence.endDate) &&
                absence.isFullDay &&
                !absence.isCounted
        }

    private const val WEEK_LAST_DAY_OFFSET = 6L
    private const val DAYS_IN_WEEK = 7
    private const val MID_WEEK_OFFSET = 3L
}
