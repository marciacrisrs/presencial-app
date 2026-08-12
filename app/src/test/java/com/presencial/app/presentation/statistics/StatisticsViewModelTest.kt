package com.presencial.app.presentation.statistics

import app.cash.turbine.test
import com.presencial.app.data.export.PdfExporter
import com.presencial.app.domain.usecase.GetStatisticsUseCase
import com.presencial.app.domain.usecase.StatisticsData
import com.presencial.app.domain.util.TimeProvider
import com.presencial.app.util.MainDispatcherExtension
import com.presencial.app.util.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.io.OutputStream
import java.time.YearMonth

class StatisticsViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val getStatisticsUseCase = mockk<GetStatisticsUseCase>()
    private val pdfExporter = mockk<PdfExporter>()
    private val timeProvider = mockk<TimeProvider>()
    private lateinit var viewModel: StatisticsViewModel

    @BeforeEach
    fun setup() {
        every { timeProvider.currentMonth() } returns YearMonth.of(2026, 8)
        every { getStatisticsUseCase(any()) } returns flowOf(TestDataFactory.createStatisticsData())
        viewModel = StatisticsViewModel(getStatisticsUseCase, pdfExporter, timeProvider)
    }

    @Test
    fun `statistics should reflect use case flow`() = runTest {
        val statsData = TestDataFactory.createStatisticsData()
        every { getStatisticsUseCase(2026) } returns flowOf(statsData)

        viewModel = StatisticsViewModel(getStatisticsUseCase, pdfExporter, timeProvider)

        viewModel.statistics.test {
            assertEquals(statsData, awaitItem())
        }
    }

    @Test
    fun `previousYear should request statistics for prior year`() = runTest {
        val stats2025 = TestDataFactory.createStatisticsData(selectedYear = 2025)
        every { getStatisticsUseCase(2026) } returns flowOf(TestDataFactory.createStatisticsData())
        every { getStatisticsUseCase(2025) } returns flowOf(stats2025)

        viewModel = StatisticsViewModel(getStatisticsUseCase, pdfExporter, timeProvider)

        viewModel.statistics.test {
            assertEquals(2026, awaitItem()?.selectedYear)
            viewModel.previousYear()
            assertEquals(2025, awaitItem()?.selectedYear)
        }
    }

    @Test
    fun `exportPdf should return failure if statistics are null`() {
        val outputStream = mockk<OutputStream>()
        val result = viewModel.exportPdf(outputStream)
        assertTrue(result.isFailure)
        assertEquals("Sem dados", result.exceptionOrNull()?.message)
    }

    @Test
    fun `exportPdf should call pdfExporter if statistics are available`() = runTest {
        val statsData = StatisticsData(
            selectedYear = 2026,
            monthlySummaries = listOf(TestDataFactory.createMonthlySummary()),
            averageAchieved = 50f,
            totalPresencial = 10,
            totalHomeOffice = 10,
            longestStreak = 5,
            currentStreak = 2,
            weeklySummaries = emptyList(),
            annualSummary = TestDataFactory.createAnnualSummary(),
            heatmapDays = emptyList()
        )
        every { getStatisticsUseCase(2026) } returns flowOf(statsData)
        viewModel = StatisticsViewModel(getStatisticsUseCase, pdfExporter, timeProvider)

        val outputStream = mockk<OutputStream>()
        every { pdfExporter.exportStatistics(any(), any(), any(), any(), any()) } returns Result.success(Unit)

        viewModel.statistics.test {
            assertEquals(statsData, awaitItem())

            val result = viewModel.exportPdf(outputStream)

            assertTrue(result.isSuccess)
            verify {
                pdfExporter.exportStatistics(
                    outputStream = outputStream,
                    summaries = statsData.monthlySummaries,
                    averageAchieved = statsData.averageAchieved,
                    totalPresencial = statsData.totalPresencial,
                    totalHomeOffice = statsData.totalHomeOffice
                )
            }
        }
    }

    @Test
    fun `exportPdf should return failure if pdfExporter fails`() = runTest {
        val statsData = TestDataFactory.createStatisticsData()
        every { getStatisticsUseCase(2026) } returns flowOf(statsData)
        viewModel = StatisticsViewModel(getStatisticsUseCase, pdfExporter, timeProvider)

        val outputStream = mockk<OutputStream>()
        val exception = Exception("PDF error")
        every { pdfExporter.exportStatistics(any(), any(), any(), any(), any()) } returns Result.failure(exception)

        viewModel.statistics.test {
            awaitItem()
            val result = viewModel.exportPdf(outputStream)
            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
        }
    }
}
