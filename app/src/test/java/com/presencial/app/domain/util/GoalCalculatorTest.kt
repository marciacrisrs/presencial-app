package com.presencial.app.domain.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GoalCalculatorTest {

    @Test
    fun `calculateRequiredDays usa ceil`() {
        assertEquals(9, GoalCalculator.calculateRequiredDays(22, 40))
        assertEquals(11, GoalCalculator.calculateRequiredDays(22, 50))
        assertEquals(1, GoalCalculator.calculateRequiredDays(1, 40))
    }

    @Test
    fun `calculateRequiredDays retorna zero para entradas invalidas`() {
        assertEquals(0, GoalCalculator.calculateRequiredDays(0, 40))
        assertEquals(0, GoalCalculator.calculateRequiredDays(22, 0))
        assertEquals(0, GoalCalculator.calculateRequiredDays(-10, 40))
        assertEquals(0, GoalCalculator.calculateRequiredDays(22, -10))
    }

    @Test
    fun `calculateAchievedPercentage com metas invalidas`() {
        assertEquals(100f, GoalCalculator.calculateAchievedPercentage(5, 0))
        assertEquals(100f, GoalCalculator.calculateAchievedPercentage(5, -1))
    }

    @Test
    fun `calculateAchievedPercentage nao ultrapassa 100`() {
        assertEquals(100f, GoalCalculator.calculateAchievedPercentage(20, 10))
    }

    @Test
    fun calculateAchievedPercentage() {
        assertEquals(50f, GoalCalculator.calculateAchievedPercentage(5, 10), 0.01f)
        assertEquals(100f, GoalCalculator.calculateAchievedPercentage(12, 10), 0.01f)
        assertEquals(100f, GoalCalculator.calculateAchievedPercentage(0, 0), 0.01f)
    }

    @Test
    fun calculateRemainingDays() {
        assertEquals(3, GoalCalculator.calculateRemainingDays(6, 9))
        assertEquals(0, GoalCalculator.calculateRemainingDays(10, 9))
    }

    @Test
    fun calculateProgressFraction() {
        assertEquals(0.5f, GoalCalculator.calculateProgressFraction(5, 10), 0.01f)
        assertEquals(1f, GoalCalculator.calculateProgressFraction(15, 10), 0.01f)
    }
}
