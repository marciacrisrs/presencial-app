package com.presencial.app.domain.util

import com.presencial.app.domain.usecase.SmartMessageParams
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gera mensagens contextuais do dashboard a partir de modelos fixos.
 */
@Singleton
class SmartMessageEngine @Inject constructor(
    private val texts: SmartMessageTextProvider
) {

    fun generate(params: SmartMessageParams): String {
        val remaining = params.remainingDays
        val remainingWorkdays = params.remainingWorkdays

        return when {
            params.requiredDays <= 0 ->
                texts.configureRequiredPercentage()

            params.completedDays >= params.requiredDays ->
                texts.goalMetCelebration()

            remaining > remainingWorkdays && remainingWorkdays > 0 ->
                texts.mustAttendAllRemaining()

            params.weeklyRequiredDays > 0 &&
                params.weeklyCompletedDays < params.weeklyRequiredDays ->
                texts.weeklyRequired(params.weeklyRequiredDays)

            remainingWorkdays > remaining * SAFETY_MARGIN &&
                remainingWorkdays > 0 &&
                remaining > 0 ->
                texts.homeOfficeUntilFriday()

            remaining <= CLOSE_TO_GOAL_THRESHOLD && remaining > 0 ->
                texts.closeToGoal(remaining)

            params.achievedPercentage < LOW_PROGRESS_THRESHOLD ->
                texts.monthStartSuggestion()

            params.projectedMonthPercentage > 0 ->
                texts.projectedMonthEnd(params.projectedMonthPercentage)

            else ->
                texts.remainingDays(remaining)
        }
    }

    companion object {
        private const val SAFETY_MARGIN = 2
        private const val CLOSE_TO_GOAL_THRESHOLD = 3
        private const val LOW_PROGRESS_THRESHOLD = 30f
    }
}
