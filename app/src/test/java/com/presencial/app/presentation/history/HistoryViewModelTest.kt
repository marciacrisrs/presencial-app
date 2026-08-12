package com.presencial.app.presentation.history

import app.cash.turbine.test
import com.presencial.app.domain.model.HistoryMonthData
import com.presencial.app.domain.usecase.GetHistoryUseCase
import com.presencial.app.domain.usecase.GetWeeklyPolicySummaryUseCase
import com.presencial.app.util.FakeTimeProvider
import com.presencial.app.util.MainDispatcherExtension
import com.presencial.app.util.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.YearMonth

class HistoryViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val getHistoryUseCase = mockk<GetHistoryUseCase>()
    private val getWeeklyPolicySummaryUseCase = mockk<GetWeeklyPolicySummaryUseCase>()
    private val timeProvider = FakeTimeProvider()

    @BeforeEach
    fun setup() {
        timeProvider.setToday(YearMonth.of(2026, 8).atDay(1))
        every { getWeeklyPolicySummaryUseCase(any()) } returns flowOf(emptyList())
    }

    @Test
    fun `historyMonths should reflect use case flow`() = runTest {
        val historyData = listOf(
            HistoryMonthData(
                summary = TestDataFactory.createMonthlySummary(),
                autoCheckInDays = 2
            )
        )
        every { getHistoryUseCase() } returns flowOf(historyData)

        val viewModel = HistoryViewModel(
            getHistoryUseCase,
            getWeeklyPolicySummaryUseCase,
            timeProvider
        )

        viewModel.historyMonths.test {
            assertEquals(historyData, awaitItem())
        }
    }
}
