package com.presencial.app.widget

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Month
import java.time.YearMonth
import java.util.Locale

class WidgetInfoTest {

    private val locale = Locale.forLanguageTag("pt-BR")

    private fun createInfo(
        completed: Int = 5,
        required: Int = 10,
        remaining: Int = 5,
        remainingWorkdays: Int = 8,
        achievedPercentage: Int = 50,
        todayIsPresencial: Boolean = false,
        todayIsWorkday: Boolean = true,
        yearMonth: YearMonth = YearMonth.of(2026, Month.AUGUST)
    ) = WidgetInfo.create(
        completed = completed,
        required = required,
        remaining = remaining,
        remainingWorkdays = remainingWorkdays,
        achievedPercentage = achievedPercentage,
        todayIsPresencial = todayIsPresencial,
        todayIsWorkday = todayIsWorkday,
        yearMonth = yearMonth,
        locale = locale
    )

    @Test
    fun `should calculate progress fraction correctly`() {
        val info = createInfo()

        assertEquals(0.5f, info.progressFraction)
        assertEquals(5, info.completed)
        assertEquals(10, info.required)
        assertEquals(5, info.remaining)
        assertEquals("AGOSTO", info.monthName)
        assertEquals(WidgetStatus.ON_TRACK, info.status)
    }

    @Test
    fun `should handle zero required days`() {
        val info = createInfo(required = 0, remaining = 0)

        assertEquals(1f, info.progressFraction)
        assertEquals(WidgetStatus.NO_GOAL, info.status)
    }

    @Test
    fun `should mark goal met when remaining is zero`() {
        val info = createInfo(completed = 10, required = 10, remaining = 0)

        assertEquals(WidgetStatus.GOAL_MET, info.status)
    }

    @Test
    fun `should mark behind when remaining exceeds workdays`() {
        val info = createInfo(remaining = 8, remainingWorkdays = 3)

        assertEquals(WidgetStatus.BEHIND, info.status)
    }

    @Test
    fun `should handle different month names for all 12 months`() {
        val expected = listOf(
            "JANEIRO", "FEVEREIRO", "MARÇO", "ABRIL", "MAIO", "JUNHO",
            "JULHO", "AGOSTO", "SETEMBRO", "OUTUBRO", "NOVEMBRO", "DEZEMBRO"
        )

        Month.entries.forEachIndexed { index, month ->
            val info = createInfo(yearMonth = YearMonth.of(2026, month))
            assertEquals(expected[index], info.monthName)
        }
    }

    @Test
    fun `should handle different locales`() {
        val info = WidgetInfo.create(
            completed = 1,
            required = 1,
            remaining = 0,
            remainingWorkdays = 1,
            achievedPercentage = 100,
            todayIsPresencial = true,
            todayIsWorkday = true,
            yearMonth = YearMonth.of(2026, Month.AUGUST),
            locale = Locale.US
        )
        assertEquals("AUGUST", info.monthName)
    }

    @Test
    fun `resolveStatus should cover all branches`() {
        assertEquals(WidgetStatus.NO_GOAL, WidgetInfo.resolveStatus(0, 1, 5))
        assertEquals(WidgetStatus.GOAL_MET, WidgetInfo.resolveStatus(10, 0, 5))
        assertEquals(WidgetStatus.BEHIND, WidgetInfo.resolveStatus(10, 6, 3))
        assertEquals(WidgetStatus.ON_TRACK, WidgetInfo.resolveStatus(10, 3, 8))
    }
}
