package com.presencial.app.presentation.calendar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.presencial.app.domain.model.DayInfo
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.ui.theme.PresencialTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class CalendarScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<CalendarViewModel>(relaxed = true)

    private val selectedMonth = MutableStateFlow(YearMonth.of(2026, 8))
    private val calendarDays = MutableStateFlow<List<DayInfo>>(emptyList())
    private val selectedDay = MutableStateFlow<DayInfo?>(null)

    @Before
    fun setup() {
        every { viewModel.selectedMonth } returns selectedMonth
        every { viewModel.calendarDays } returns calendarDays
        every { viewModel.selectedDay } returns selectedDay
    }

    @Test
    fun calendarScreen_displaysInitialContent() {
        startCalendarScreen()

        composeTestRule.onNodeWithText("Calendário").assertIsDisplayed()
        composeTestRule.onNodeWithText("Agosto 2026").assertIsDisplayed()
    }

    @Test
    fun clickingPreviousMonth_triggersViewModelAction() {
        startCalendarScreen()

        composeTestRule.onNodeWithContentDescription("Mês anterior").performClick()
        verify { viewModel.previousMonth() }
    }

    @Test
    fun clickingNextMonth_triggersViewModelAction() {
        startCalendarScreen()

        composeTestRule.onNodeWithContentDescription("Próximo mês").performClick()
        verify { viewModel.nextMonth() }
    }

    @Test
    fun clickingDay_triggersViewModelAction() {
        val day = DayInfo(
            date = LocalDate.of(2026, 8, 15),
            status = DayStatus.FUTURO,
            isWorkday = true,
            isHoliday = false,
            isEditable = true
        )
        calendarDays.value = listOf(day)
        
        startCalendarScreen()

        composeTestRule.onNodeWithText("15").performClick()
        verify { viewModel.selectDay(any()) }
    }

    @Test
    fun selectedDayPresent_displaysEditorDialog() {
        val day = DayInfo(
            date = LocalDate.of(2026, 8, 15),
            status = DayStatus.FUTURO,
            isWorkday = true,
            isHoliday = false,
            isEditable = true
        )
        selectedDay.value = day
        
        startCalendarScreen()

        composeTestRule.onNodeWithText("Editar — 15 de agosto").assertIsDisplayed()
    }

    @Test
    fun dialogActions_triggerViewModelActions() {
        val day = DayInfo(
            date = LocalDate.of(2026, 8, 15),
            status = DayStatus.HOME_OFFICE,
            isWorkday = true,
            isHoliday = false,
            isEditable = true
        )
        selectedDay.value = day
        
        startCalendarScreen()

        // Verify "Salvar"
        composeTestRule.onNodeWithText("Salvar").performClick()
        verify { viewModel.updateDayStatus(any()) }

        // Verify "Limpar"
        composeTestRule.onNodeWithText("Limpar").performClick()
        verify { viewModel.updateDayStatus(DayStatus.FUTURO) }

        // Verify "Cancelar"
        composeTestRule.onNodeWithText("Cancelar").performClick()
        verify { viewModel.dismissDayEditor() }
    }

    @Test
    fun clickingAbsences_triggersNavigation() {
        var navigated = false
        startCalendarScreen(onNavigateToAbsences = { navigated = true })

        composeTestRule.onNodeWithText("Ausências").performClick()
        assert(navigated)
    }

    private fun startCalendarScreen(onNavigateToAbsences: () -> Unit = {}) {
        composeTestRule.setContent {
            PresencialTheme {
                CalendarScreen(
                    viewModel = viewModel,
                    onNavigateToAbsences = onNavigateToAbsences
                )
            }
        }
    }
}
