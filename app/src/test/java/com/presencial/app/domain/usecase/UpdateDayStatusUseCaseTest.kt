package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.MonthlySummaryRepository
import com.presencial.app.domain.widget.WidgetRefresher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import java.time.YearMonth
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateDayStatusUseCaseTest {

    private val checkInRepository: CheckInRepository = mockk()
    private val monthlySummaryRepository: MonthlySummaryRepository = mockk()
    private val widgetRefresher: WidgetRefresher = mockk()
    private lateinit var useCase: UpdateDayStatusUseCase

    @BeforeEach
    fun setup() {
        coEvery { widgetRefresher.refresh() } returns Unit
        useCase = UpdateDayStatusUseCase(
            checkInRepository,
            monthlySummaryRepository,
            widgetRefresher
        )
    }

    @Test
    fun `given presencial status, when invoke, then saveCheckIn is called`() = runTest {
        // Arrange
        val date = LocalDate.of(2026, 8, 6)
        coEvery { checkInRepository.saveCheckIn(any(), any(), any()) } returns Unit
        coEvery { monthlySummaryRepository.refreshSummary(any()) } returns Unit

        // Act
        useCase(date, DayStatus.PRESENCIAL)

        // Assert
        coVerify { checkInRepository.saveCheckIn(date, DayStatus.PRESENCIAL, "MANUAL") }
        coVerify { monthlySummaryRepository.refreshSummary(YearMonth.of(2026, 8)) }
        coVerify { widgetRefresher.refresh() }
    }

    @Test
    fun `given home office status, when invoke, then saveCheckIn is called`() = runTest {
        // Arrange
        val date = LocalDate.of(2026, 8, 6)
        coEvery { checkInRepository.saveCheckIn(any(), any(), any()) } returns Unit
        coEvery { monthlySummaryRepository.refreshSummary(any()) } returns Unit

        // Act
        useCase(date, DayStatus.HOME_OFFICE)

        // Assert
        coVerify { checkInRepository.saveCheckIn(date, DayStatus.HOME_OFFICE, "MANUAL") }
        coVerify { monthlySummaryRepository.refreshSummary(YearMonth.of(2026, 8)) }
        coVerify { widgetRefresher.refresh() }
    }

    @Test
    fun `given other status, when invoke, then deleteCheckIn is called`() = runTest {
        // Arrange
        val date = LocalDate.of(2026, 8, 6)
        coEvery { checkInRepository.deleteCheckIn(any()) } returns Unit
        coEvery { monthlySummaryRepository.refreshSummary(any()) } returns Unit

        // Act
        useCase(date, DayStatus.FERIADO)

        // Assert
        coVerify { checkInRepository.deleteCheckIn(date) }
        coVerify { monthlySummaryRepository.refreshSummary(YearMonth.of(2026, 8)) }
        coVerify { widgetRefresher.refresh() }
    }
}
