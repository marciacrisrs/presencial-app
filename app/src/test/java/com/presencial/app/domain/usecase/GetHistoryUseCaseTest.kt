package com.presencial.app.domain.usecase

import app.cash.turbine.test
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.MonthlySummary
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.MonthlySummaryRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.util.FakeTimeProvider
import com.presencial.app.util.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.YearMonth
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetHistoryUseCaseTest {

    private val monthlySummaryRepository: MonthlySummaryRepository = mockk()
    private val checkInRepository: CheckInRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val timeProvider = FakeTimeProvider()
    private lateinit var useCase: GetHistoryUseCase

    @BeforeEach
    fun setup() {
        every { checkInRepository.observeAllCheckIns() } returns flowOf(emptyList())
        useCase = GetHistoryUseCase(
            monthlySummaryRepository,
            checkInRepository,
            settingsRepository,
            timeProvider
        )
    }

    @Test
    fun `given current month missing in summaries, when invoke, then include current month`() = runTest {
        val currentMonth = YearMonth.of(2026, 8)
        timeProvider.setNow(currentMonth.atDay(1).atStartOfDay())

        val summaries = emptyList<MonthlySummary>()
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = false)

        every { monthlySummaryRepository.observeAllSummaries() } returns flowOf(summaries)
        every { settingsRepository.settings } returns flowOf(settings)

        useCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            val added = result[0].summary
            assertEquals(currentMonth, added.yearMonth)
            assertEquals(21, added.workdays)
            assertEquals(9, added.requiredDays)
            assertEquals(0, result[0].autoCheckInDays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given current month missing and saturdays count, when invoke, then calculate workdays correctly`() = runTest {
        val currentMonth = YearMonth.of(2026, 8)
        timeProvider.setNow(currentMonth.atDay(1).atStartOfDay())

        val summaries = emptyList<MonthlySummary>()
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = true)

        every { monthlySummaryRepository.observeAllSummaries() } returns flowOf(summaries)
        every { settingsRepository.settings } returns flowOf(settings)

        useCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            val added = result[0].summary
            assertEquals(26, added.workdays)
            assertEquals(11, added.requiredDays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given current month already in summaries, when invoke, then return summaries as is`() = runTest {
        val currentMonth = YearMonth.of(2026, 8)
        timeProvider.setNow(currentMonth.atDay(1).atStartOfDay())

        val existingSummary = TestDataFactory.createMonthlySummary(yearMonth = currentMonth)
        val summaries = listOf(existingSummary)
        val settings = AppSettings()

        every { monthlySummaryRepository.observeAllSummaries() } returns flowOf(summaries)
        every { settingsRepository.settings } returns flowOf(settings)

        useCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(existingSummary, result[0].summary)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when auto geofence check-ins exist, then count them per month`() = runTest {
        val month = YearMonth.of(2026, 8)
        val summary = TestDataFactory.createMonthlySummary(yearMonth = month)
        val autoCheckIn = TestDataFactory.createCheckIn(
            date = month.atDay(6),
            source = com.presencial.app.domain.model.CheckInSource.AUTO_GEOFENCE
        )

        every { monthlySummaryRepository.observeAllSummaries() } returns flowOf(listOf(summary))
        every { checkInRepository.observeAllCheckIns() } returns flowOf(listOf(autoCheckIn))
        every { settingsRepository.settings } returns flowOf(AppSettings())

        useCase().test {
            val result = awaitItem()
            assertEquals(1, result[0].autoCheckInDays)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
