package com.presencial.app.domain.usecase

import app.cash.turbine.test
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.MonthlySummary
import com.presencial.app.domain.repository.MonthlySummaryRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.util.FakeTimeProvider
import com.presencial.app.util.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.YearMonth
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetHistoryUseCaseTest {

    private val monthlySummaryRepository: MonthlySummaryRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val timeProvider = FakeTimeProvider()
    private lateinit var useCase: GetHistoryUseCase

    @BeforeEach
    fun setup() {
        useCase = GetHistoryUseCase(
            monthlySummaryRepository,
            settingsRepository,
            timeProvider
        )
    }

    @Test
    fun `given current month missing in summaries, when invoke, then include current month`() = runTest {
        // Arrange
        val currentMonth = YearMonth.of(2026, 8)
        timeProvider.setNow(currentMonth.atDay(1).atStartOfDay())

        val summaries = emptyList<MonthlySummary>()
        val settings = AppSettings()

        every { monthlySummaryRepository.observeAllSummaries() } returns flowOf(summaries)
        every { settingsRepository.settings } returns flowOf(settings)

        // Act & Assert
        useCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(currentMonth, result[0].yearMonth)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given current month already in summaries, when invoke, then return summaries as is`() = runTest {
        // Arrange
        val currentMonth = YearMonth.of(2026, 8)
        timeProvider.setNow(currentMonth.atDay(1).atStartOfDay())

        val existingSummary = TestDataFactory.createMonthlySummary(yearMonth = currentMonth)
        val summaries = listOf(existingSummary)
        val settings = AppSettings()

        every { monthlySummaryRepository.observeAllSummaries() } returns flowOf(summaries)
        every { settingsRepository.settings } returns flowOf(settings)

        // Act & Assert
        useCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(existingSummary, result[0])
            cancelAndIgnoreRemainingEvents()
        }
    }
}
