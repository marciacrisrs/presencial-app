package com.presencial.app.widget

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.Month
import java.time.YearMonth
import java.util.Locale

class WidgetInfoTest {

    private val locale = Locale.forLanguageTag("pt-BR")

    @Test
    fun `should calculate progress fraction correctly`() {
        val yearMonth = YearMonth.of(2026, Month.AUGUST)
        val info = WidgetInfo.create(
            completed = 5,
            required = 10,
            remaining = 5,
            yearMonth = yearMonth,
            locale = locale
        )

        assertEquals(0.5f, info.progressFraction)
        assertEquals(5, info.completed)
        assertEquals(10, info.required)
        assertEquals(5, info.remaining)
        assertEquals("AGOSTO", info.monthName)
    }

    @Test
    fun `should handle zero required days`() {
        val yearMonth = YearMonth.of(2026, Month.AUGUST)
        val info = WidgetInfo.create(
            completed = 5,
            required = 0,
            remaining = 0,
            yearMonth = yearMonth,
            locale = locale
        )

        assertEquals(1f, info.progressFraction)
        assertEquals(0, info.required)
    }

    @Test
    fun `should handle different month names for all 12 months`() {
        val months = Month.entries.toTypedArray()
        val locale = Locale.forLanguageTag("pt-BR")
        
        val expected = listOf(
            "JANEIRO", "FEVEREIRO", "MARÇO", "ABRIL", "MAIO", "JUNHO",
            "JULHO", "AGOSTO", "SETEMBRO", "OUTUBRO", "NOVEMBRO", "DEZEMBRO"
        )

        months.forEachIndexed { index, month ->
            val yearMonth = YearMonth.of(2026, month)
            val info = WidgetInfo.create(1, 1, 0, yearMonth, locale)
            assertEquals(expected[index], info.monthName)
        }
    }

    @Test
    fun `should handle different locales`() {
        val yearMonth = YearMonth.of(2026, Month.AUGUST)
        val info = WidgetInfo.create(1, 1, 0, yearMonth, Locale.US)
        assertEquals("AUGUST", info.monthName)
    }

    @Test
    fun `should cover WidgetSize enum`() {
        assertEquals(3, WidgetSize.entries.size)
        assertEquals(WidgetSize.SMALL, WidgetSize.valueOf("SMALL"))
        assertEquals(WidgetSize.MEDIUM, WidgetSize.valueOf("MEDIUM"))
        assertEquals(WidgetSize.LARGE, WidgetSize.valueOf("LARGE"))
        WidgetSize.entries.forEach {
            assertNotNull(it.name)
        }
    }
    
    @Test
    fun `should cover WidgetInfo data class copy and equals`() {
        val info1 = WidgetInfo(1, 2, 1, 0.5f, "TEST")
        val info2 = info1.copy(completed = 2)
        assertEquals(2, info2.completed)
        assertEquals(info1, info1)
        assertEquals(info1.hashCode(), info1.hashCode())
        assertNotNull(info1.toString())
    }
}
