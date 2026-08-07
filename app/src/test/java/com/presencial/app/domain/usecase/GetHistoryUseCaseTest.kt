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
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = false)

        every { monthlySummaryRepository.observeAllSummaries() } returns flowOf(summaries)
        every { settingsRepository.settings } returns flowOf(settings)

        // Act & Assert
        useCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            val added = result[0]
            assertEquals(currentMonth, added.yearMonth)
            assertEquals(21, added.workdays) // Aug 2026 has 21 workdays (no sats)
            assertEquals(9, added.requiredDays) // 40% of 21 is 8.4 -> 9
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given current month missing and saturdays count, when invoke, then calculate workdays correctly`() = runTest {
        // Arrange
        val currentMonth = YearMonth.of(2026, 8)
        timeProvider.setNow(currentMonth.atDay(1).atStartOfDay())

        val summaries = emptyList<MonthlySummary>()
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = true)

        every { monthlySummaryRepository.observeAllSummaries() } returns flowOf(summaries)
        every { settingsRepository.settings } returns flowOf(settings)

        // Act & Assert
        useCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            val added = result[0]
            assertEquals(26, added.workdays) // Aug 2026 has 21 + 5 Saturdays = 26
            assertEquals(11, added.requiredDays) // 40% of 26 is 10.4 -> 11
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
