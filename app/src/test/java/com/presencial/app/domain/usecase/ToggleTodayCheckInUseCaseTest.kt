package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.MonthlySummaryRepository
import com.presencial.app.domain.widget.WidgetRefresher
import com.presencial.app.util.FakeTimeProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import java.time.YearMonth
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ToggleTodayCheckInUseCaseTest {

    private val checkInRepository: CheckInRepository = mockk()
    private val monthlySummaryRepository: MonthlySummaryRepository = mockk()
    private val widgetRefresher: WidgetRefresher = mockk()
    private val timeProvider = FakeTimeProvider()
    private lateinit var useCase: ToggleTodayCheckInUseCase

    @BeforeEach
    fun setup() {
        coEvery { widgetRefresher.refresh() } returns Unit
        useCase = ToggleTodayCheckInUseCase(
            checkInRepository,
            monthlySummaryRepository,
            timeProvider,
            widgetRefresher
        )
    }

    @Test
    fun `given markPresencial is true, when invoke, then saveCheckIn is called`() = runTest {
        // Arrange
        val today = LocalDate.of(2026, 8, 6)
        timeProvider.setToday(today)
        coEvery { checkInRepository.saveCheckIn(any(), any(), any()) } returns Unit
        coEvery { monthlySummaryRepository.refreshSummary(any()) } returns Unit

        // Act
        useCase(markPresencial = true)

        // Assert
        coVerify { checkInRepository.saveCheckIn(today, DayStatus.PRESENCIAL, "MANUAL") }
        coVerify { monthlySummaryRepository.refreshSummary(YearMonth.of(2026, 8)) }
    }

    @Test
    fun `given markPresencial is false, when invoke, then deleteCheckIn is called`() = runTest {
        // Arrange
        val today = LocalDate.of(2026, 8, 6)
        timeProvider.setToday(today)
        coEvery { checkInRepository.deleteCheckIn(any()) } returns Unit
        coEvery { monthlySummaryRepository.refreshSummary(any()) } returns Unit

        // Act
        useCase(markPresencial = false)

        // Assert
        coVerify { checkInRepository.deleteCheckIn(today) }
        coVerify { monthlySummaryRepository.refreshSummary(YearMonth.of(2026, 8)) }
    }

    @Test
    fun `given specific date, when invoke, then use that date instead of today`() = runTest {
        // Arrange
        val today = LocalDate.of(2026, 8, 6)
        val specificDate = LocalDate.of(2026, 8, 5)
        timeProvider.setToday(today)
        coEvery { checkInRepository.saveCheckIn(any(), any(), any()) } returns Unit
        coEvery { monthlySummaryRepository.refreshSummary(any()) } returns Unit

        // Act
        useCase(date = specificDate, markPresencial = true)

        // Assert
        coVerify { checkInRepository.saveCheckIn(specificDate, DayStatus.PRESENCIAL, "MANUAL") }
        coVerify { monthlySummaryRepository.refreshSummary(YearMonth.of(2026, 8)) }
    }
}
