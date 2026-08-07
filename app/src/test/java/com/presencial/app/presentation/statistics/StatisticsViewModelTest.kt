package com.presencial.app.presentation.statistics

import app.cash.turbine.test
import com.presencial.app.data.export.PdfExporter
import com.presencial.app.domain.usecase.GetStatisticsUseCase
import com.presencial.app.domain.usecase.StatisticsData
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

class StatisticsViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val getStatisticsUseCase = mockk<GetStatisticsUseCase>()
    private val pdfExporter = mockk<PdfExporter>()
    private lateinit var viewModel: StatisticsViewModel

    @BeforeEach
    fun setup() {
        every { getStatisticsUseCase() } returns flowOf(TestDataFactory.createStatisticsData())
        viewModel = StatisticsViewModel(getStatisticsUseCase, pdfExporter)
    }

    @Test
    fun `statistics should reflect use case flow`() = runTest {
        val statsData = TestDataFactory.createStatisticsData()
        every { getStatisticsUseCase() } returns flowOf(statsData)
        
        viewModel = StatisticsViewModel(getStatisticsUseCase, pdfExporter)

        viewModel.statistics.test {
            assertEquals(statsData, awaitItem())
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
            monthlySummaries = listOf(TestDataFactory.createMonthlySummary()),
            averageAchieved = 50f,
            totalPresencial = 10,
            totalHomeOffice = 10,
            longestStreak = 5,
            currentStreak = 2
        )
        every { getStatisticsUseCase() } returns flowOf(statsData)
        viewModel = StatisticsViewModel(getStatisticsUseCase, pdfExporter)
        
        val outputStream = mockk<OutputStream>()
        every { pdfExporter.exportStatistics(any(), any(), any(), any(), any()) } returns Result.success(Unit)

        // Ensure StateFlow is updated
        viewModel.statistics.test {
            assertEquals(statsData, awaitItem())
            
            val result = viewModel.exportPdf(outputStream)

            assertTrue(result.isSuccess)
            verify { pdfExporter.exportStatistics(
                outputStream = outputStream,
                summaries = statsData.monthlySummaries,
                averageAchieved = statsData.averageAchieved,
                totalPresencial = statsData.totalPresencial,
                totalHomeOffice = statsData.totalHomeOffice
            ) }
        }
    }
}
