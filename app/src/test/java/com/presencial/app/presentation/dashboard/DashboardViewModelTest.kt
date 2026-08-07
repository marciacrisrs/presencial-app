package com.presencial.app.presentation.dashboard

import app.cash.turbine.test
import com.presencial.app.domain.usecase.GetDashboardDataUseCase
import com.presencial.app.domain.usecase.ToggleTodayCheckInUseCase
import com.presencial.app.domain.util.TimeProvider
import com.presencial.app.util.MainDispatcherExtension
import com.presencial.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.LocalDate
import java.time.YearMonth

class DashboardViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val getDashboardDataUseCase = mockk<GetDashboardDataUseCase>()
    private val toggleTodayCheckInUseCase = mockk<ToggleTodayCheckInUseCase>()
    private val timeProvider = mockk<TimeProvider>()

    private lateinit var viewModel: DashboardViewModel

    private val today = LocalDate.of(2026, 8, 6)

    @BeforeEach
    fun setup() {
        every { timeProvider.today() } returns today
        every { timeProvider.currentMonth() } returns YearMonth.from(today)
        every { getDashboardDataUseCase(any()) } returns flowOf(TestDataFactory.createDashboardData())
        viewModel = DashboardViewModel(getDashboardDataUseCase, toggleTodayCheckInUseCase, timeProvider)
    }

    @Test
    fun `dashboardData should reflect use case flow`() = runTest {
        val data = TestDataFactory.createDashboardData()
        every { getDashboardDataUseCase(any()) } returns flowOf(data)
        
        // Re-init to use the new flow
        viewModel = DashboardViewModel(getDashboardDataUseCase, toggleTodayCheckInUseCase, timeProvider)

        viewModel.dashboardData.test {
            assertEquals(data, awaitItem())
        }
    }

    @Test
    fun `toggleTodayCheckIn should call use case`() = runTest {
        coEvery { toggleTodayCheckInUseCase(markPresencial = true) } returns Unit

        viewModel.toggleTodayCheckIn(true)

        coVerify { toggleTodayCheckInUseCase(markPresencial = true) }
    }

    @Test
    fun `markYesterdayPresencial should call use case with yesterday date`() = runTest {
        val yesterday = today.minusDays(1)
        coEvery { toggleTodayCheckInUseCase(date = yesterday, markPresencial = true) } returns Unit

        viewModel.markYesterdayPresencial()

        coVerify { toggleTodayCheckInUseCase(date = yesterday, markPresencial = true) }
    }
}
