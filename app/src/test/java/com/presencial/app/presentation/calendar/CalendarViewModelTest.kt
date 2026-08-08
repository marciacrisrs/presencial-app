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
    fun `calendarDays should update when month changes multiple times`() = runTest {
        val month1 = YearMonth.now()
        val month2 = month1.plusMonths(1)
        val month3 = month2.plusMonths(1)
        
        val days1 = listOf(DayInfo(month1.atDay(1), DayStatus.PRESENCIAL, true, false))
        val days2 = listOf(DayInfo(month2.atDay(1), DayStatus.PRESENCIAL, true, false))
        val days3 = listOf(DayInfo(month3.atDay(1), DayStatus.PRESENCIAL, true, false))

        every { getMonthCalendarUseCase(month1) } returns flowOf(days1)
        every { getMonthCalendarUseCase(month2) } returns flowOf(days2)
        every { getMonthCalendarUseCase(month3) } returns flowOf(days3)

        viewModel.calendarDays.test {
            assertEquals(days1, awaitItem())

            viewModel.nextMonth()
            assertEquals(days2, awaitItem())

            viewModel.nextMonth()
            assertEquals(days3, awaitItem())

            viewModel.previousMonth()
            assertEquals(days2, awaitItem())
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
    fun `updateDayStatus should return early when no day is selected`() = runTest {
        viewModel.updateDayStatus(DayStatus.PRESENCIAL)
        coVerify(exactly = 0) { updateDayStatusUseCase(any(), any()) }
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
