package com.presencial.app.domain.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PresenceProgressPresentationTest {

    @Test
    fun `zero feitos mostra X de Y e quantos faltam`() {
        val remaining = GoalCalculator.calculateRemainingDays(completedDays = 0, requiredDays = 8)
        val copy = PresenceProgressPresentation.from(
            completedDays = 0,
            requiredDays = 8,
            remainingDays = remaining,
            policyPercentage = 40
        )

        assertEquals("0 de 8 dias presenciais", copy.daysLine)
        assertEquals("Faltam 8 dias", copy.remainingLine)
        assertEquals("Regra: 40% dos dias úteis", copy.policyLine)
        assertFalse(copy.isGoalMet)
    }

    @Test
    fun `parcialmente concluido destaca os dias que faltam`() {
        val copy = PresenceProgressPresentation.from(
            completedDays = 4,
            requiredDays = 8,
            remainingDays = 4,
            policyPercentage = 40
        )

        assertEquals("4 de 8 dias presenciais", copy.daysLine)
        assertEquals("Faltam 4 dias", copy.remainingLine)
        assertFalse(copy.isGoalMet)
    }

    @Test
    fun `meta concluida nao compete com percentual`() {
        val copy = PresenceProgressPresentation.from(
            completedDays = 8,
            requiredDays = 8,
            remainingDays = 0,
            policyPercentage = 40,
            companyName = "ACME"
        )

        assertEquals("8 de 8 dias presenciais", copy.daysLine)
        assertEquals("Meta atingida", copy.remainingLine)
        assertEquals("Regra: 40% dos dias úteis · ACME", copy.policyLine)
        assertTrue(copy.isGoalMet)
    }

    @Test
    fun `um dia restante usa singular`() {
        val copy = PresenceProgressPresentation.from(
            completedDays = 7,
            requiredDays = 8,
            remainingDays = 1,
            policyPercentage = 40
        )

        assertEquals("Falta 1 dia", copy.remainingLine)
    }

    @Test
    fun `sem meta pede configuracao`() {
        val remaining = GoalCalculator.calculateRemainingDays(completedDays = 0, requiredDays = 0)
        val copy = PresenceProgressPresentation.from(
            completedDays = 0,
            requiredDays = 0,
            remainingDays = remaining,
            policyPercentage = 40
        )

        assertEquals("0 de 0 dias presenciais", copy.daysLine)
        assertEquals("Configure sua meta", copy.remainingLine)
        assertFalse(copy.isGoalMet)
    }
}
