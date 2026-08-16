package com.presencial.app.domain.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OnboardingEligibilityTest {

    @Test
    fun `primeiro launch mostra onboarding`() {
        assertTrue(OnboardingEligibility.shouldShow(onboardingCompleted = false, hasExistingCheckIns = false))
    }

    @Test
    fun `onboarding concluido nao reaparece`() {
        assertFalse(OnboardingEligibility.shouldShow(onboardingCompleted = true, hasExistingCheckIns = false))
    }

    @Test
    fun `usuario existente com check-ins nao recebe onboarding`() {
        assertFalse(OnboardingEligibility.shouldShow(onboardingCompleted = false, hasExistingCheckIns = true))
    }

    @Test
    fun `onboarding interrompido continua visivel`() {
        assertTrue(OnboardingEligibility.shouldShow(onboardingCompleted = false, hasExistingCheckIns = false))
        assertEquals(OnboardingEligibility.STEP_REMINDER, OnboardingEligibility.coerceStep(1))
    }

    @Test
    fun `nextStep normaliza extremos`() {
        assertEquals(OnboardingEligibility.STEP_REMINDER, OnboardingEligibility.nextStep(-10))
        assertEquals(OnboardingEligibility.STEP_LOCATION, OnboardingEligibility.nextStep(Int.MAX_VALUE))
        assertEquals(OnboardingEligibility.STEP_LOCATION, OnboardingEligibility.nextStep(OnboardingEligibility.STEP_LOCATION))
        assertEquals(OnboardingEligibility.STEP_REMINDER, OnboardingEligibility.nextStep(OnboardingEligibility.STEP_GOAL))
    }
}
