package com.presencial.app.domain.util

object OnboardingEligibility {
    const val STEP_GOAL = 0
    const val STEP_REMINDER = 1
    const val STEP_LOCATION = 2
    const val STEP_COUNT = 3

    fun shouldShow(onboardingCompleted: Boolean, hasExistingCheckIns: Boolean): Boolean =
        !onboardingCompleted && !hasExistingCheckIns

    fun coerceStep(step: Int): Int = step.coerceIn(STEP_GOAL, STEP_LOCATION)

    fun nextStep(step: Int): Int {
        val current = coerceStep(step)
        return (current + 1).coerceAtMost(STEP_LOCATION)
    }
}
