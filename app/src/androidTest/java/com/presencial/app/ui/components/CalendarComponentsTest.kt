package com.presencial.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.presencial.app.domain.model.DayInfo
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.ui.theme.PresencialTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class CalendarComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun monthCalendarGrid_showsWeekdayHeaders() {
        composeTestRule.setContent {
            PresencialTheme {
                MonthCalendarGrid(
                    days = emptyList(),
                    onDayClick = {}
                )
            }
        }

        val weekDays = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
        weekDays.forEach { day ->
            composeTestRule.onNodeWithText(day).assertIsDisplayed()
        }
    }

    @Test
    fun monthCalendarGrid_showsDayNumbers() {
        val days = listOf(
            DayInfo(LocalDate.of(2024, 1, 1), DayStatus.PRESENCIAL, true, false),
            DayInfo(LocalDate.of(2024, 1, 2), DayStatus.HOME_OFFICE, true, false)
        )

        composeTestRule.setContent {
            PresencialTheme {
                MonthCalendarGrid(
                    days = days,
                    onDayClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
    }

    @Test
    fun monthCalendarGrid_clickEditableDay_triggersCallback() {
        var clickedDay: DayInfo? = null
        val targetDate = LocalDate.of(2024, 1, 1)
        val days = listOf(
            DayInfo(targetDate, DayStatus.PRESENCIAL, true, false, isEditable = true)
        )

        composeTestRule.setContent {
            PresencialTheme {
                MonthCalendarGrid(
                    days = days,
                    onDayClick = { clickedDay = it }
                )
            }
        }

        composeTestRule.onNodeWithText("1").performClick()
        assertEquals(targetDate, clickedDay?.date)
    }

    @Test
    fun monthCalendarGrid_clickNonEditableDay_doesNotTriggerCallback() {
        var clickedDay: DayInfo? = null
        val days = listOf(
            DayInfo(LocalDate.of(2024, 1, 1), DayStatus.PRESENCIAL, true, false, isEditable = false)
        )

        composeTestRule.setContent {
            PresencialTheme {
                MonthCalendarGrid(
                    days = days,
                    onDayClick = { clickedDay = it }
                )
            }
        }

        composeTestRule.onNodeWithText("1").performClick()
        assertEquals(null, clickedDay)
    }

    @Test
    fun calendarLegend_showsAllItems() {
        composeTestRule.setContent {
            PresencialTheme {
                CalendarLegend()
            }
        }

        val legendItems = listOf(
            "● 🏢 Presencial",
            "● 🏠 Home Office",
            "● ❌ Faltou",
            "● 🧡 Ausência",
            "● 🔵 Hoje",
            "● 🎉 Feriado"
        )
        legendItems.forEach { item ->
            composeTestRule.onNodeWithText(item).assertIsDisplayed()
        }
    }

    // Unit Tests for pure functions
    @Test
    fun dayColor_logic() {
        // Values from CalendarComponents.kt
        val colorGreen = Color(0xFF1B873B)
        val colorGray = Color(0xFF9AA0A6)
        val colorRedFaltou = Color(0xFFD93025).copy(alpha = 0.7f)
        val colorOrange = Color(0xFFFF8C00)
        val colorYellow = Color(0xFFF9AB00)
        val colorBlueToday = Color(0xFF1A73E8).copy(alpha = 0.3f)

        // Case: isToday = true, not marked as PRESENCIAL or ABSENCE
        assertEquals(colorBlueToday, dayColor(DayStatus.FUTURO, true))
        assertEquals(colorBlueToday, dayColor(DayStatus.HOME_OFFICE, true))
        
        // Case: isToday = true, but marked as PRESENCIAL or ABSENCE
        assertEquals(colorGreen, dayColor(DayStatus.PRESENCIAL, true))
        assertEquals(colorOrange, dayColor(DayStatus.ABSENCE, true))

        // Case: isToday = false
        assertEquals(colorGreen, dayColor(DayStatus.PRESENCIAL, false))
        assertEquals(colorGray, dayColor(DayStatus.HOME_OFFICE, false))
        assertEquals(colorYellow, dayColor(DayStatus.FERIADO, false))
        assertEquals(Color.Transparent, dayColor(DayStatus.FIM_DE_SEMANA, false))
        assertEquals(Color.Transparent, dayColor(DayStatus.FUTURO, false))
        assertEquals(colorRedFaltou, dayColor(DayStatus.FALTOU, false))
        assertEquals(colorOrange, dayColor(DayStatus.ABSENCE, false))
    }

    @Test
    fun formatMonthYear_portugueseFormatting() {
        assertEquals("Janeiro 2024", formatMonthYear(2024, 1))
        assertEquals("Maio 2024", formatMonthYear(2024, 5))
        assertEquals("Agosto 2024", formatMonthYear(2024, 8))
        assertEquals("Dezembro 2024", formatMonthYear(2024, 12))
    }
}
