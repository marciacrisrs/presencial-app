package com.presencial.app.presentation.calendar

import app.cash.turbine.test
import com.presencial.app.domain.model.DayInfo
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.usecase.GetMonthCalendarUseCase
import com.presencial.app.domain.usecase.UpdateDayStatusUseCase
import com.presencial.app.util.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val getMonthCalendarUseCase = mockk<GetMonthCalendarUseCase>()
    private val updateDayStatusUseCase = mockk<UpdateDayStatusUseCase>()

    private lateinit var viewModel: CalendarViewModel

    @BeforeEach
    fun setup() {
        every { getMonthCalendarUseCase(any()) } returns flowOf(emptyList())
        viewModel = CalendarViewModel(getMonthCalendarUseCase, updateDayStatusUseCase)
    }

    @Test
    fun `selectedMonth should start with current month`() = runTest {
        val currentMonth = YearMonth.now()
        assertEquals(currentMonth, viewModel.selectedMonth.value)
    }

    @Test
    fun `previousMonth should decrement month`() = runTest {
        val initialMonth = viewModel.selectedMonth.value
        viewModel.previousMonth()
        assertEquals(initialMonth.minusMonths(1), viewModel.selectedMonth.value)
    }

    @Test
    fun `nextMonth should increment month`() = runTest {
        val initialMonth = viewModel.selectedMonth.value
        viewModel.nextMonth()
        assertEquals(initialMonth.plusMonths(1), viewModel.selectedMonth.value)
    }

    @Test
    fun `calendarDays should reflect use case flow for selected month`() = runTest {
        val month = YearMonth.now().plusMonths(1)
        val days = listOf(
            DayInfo(
                LocalDate.of(2026, 8, 1),
                DayStatus.PRESENCIAL,
                isWorkday = true,
                isHoliday = false
            )
        )
        every { getMonthCalendarUseCase(month) } returns flowOf(days)

        viewModel.calendarDays.test {
            assertEquals(emptyList<DayInfo>(), awaitItem())
            
            viewModel.nextMonth()
            
            assertEquals(days, awaitItem())
        }
    }

    @Test
    fun `selectDay should update selectedDay if editable`() = runTest {
        val day = DayInfo(
            date = LocalDate.now(),
            status = DayStatus.PRESENCIAL,
            isWorkday = true,
            isHoliday = false,
            isEditable = true
        )
        viewModel.selectDay(day)
        assertEquals(day, viewModel.selectedDay.value)
    }

    @Test
    fun `selectDay should NOT update selectedDay if not editable`() = runTest {
        val day = DayInfo(
            date = LocalDate.now(),
            status = DayStatus.PRESENCIAL,
            isWorkday = true,
            isHoliday = false,
            isEditable = false
        )
        viewModel.selectDay(day)
        assertNull(viewModel.selectedDay.value)
    }

    @Test
    fun `dismissDayEditor should clear selectedDay`() = runTest {
        val day = DayInfo(
            date = LocalDate.now(),
            status = DayStatus.PRESENCIAL,
            isWorkday = true,
            isHoliday = false,
            isEditable = true
        )
        viewModel.selectDay(day)
        viewModel.dismissDayEditor()
        assertNull(viewModel.selectedDay.value)
    }

    @Test
    fun `updateDayStatus should call use case and clear selection`() = runTest {
        val date = LocalDate.of(2026, 8, 6)
        val day = DayInfo(
            date = date,
            status = DayStatus.PRESENCIAL,
            isWorkday = true,
            isHoliday = false,
            isEditable = true
        )
        viewModel.selectDay(day)

        coEvery { updateDayStatusUseCase(date, DayStatus.PRESENCIAL) } returns Unit

        viewModel.updateDayStatus(DayStatus.PRESENCIAL)

        coVerify { updateDayStatusUseCase(date, DayStatus.PRESENCIAL) }
        assertNull(viewModel.selectedDay.value)
    }
}
