package com.presencial.app.presentation.statistics

import app.cash.turbine.test
import com.presencial.app.data.export.CsvExporter
import com.presencial.app.data.export.ExcelExporter
import com.presencial.app.data.export.PdfExporter
import com.presencial.app.domain.model.AttendanceReport
import com.presencial.app.domain.model.AttendanceReportFooter
import com.presencial.app.domain.model.AttendanceReportRow
import com.presencial.app.domain.usecase.GetAttendanceReportUseCase
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
import java.time.LocalDate
import java.time.YearMonth

class StatisticsViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val getStatisticsUseCase = mockk<GetStatisticsUseCase>()
    private val getAttendanceReportUseCase = mockk<GetAttendanceReportUseCase>()
    private val pdfExporter = mockk<PdfExporter>()
    private val csvExporter = mockk<CsvExporter>()
    private val excelExporter = mockk<ExcelExporter>()
    private val timeProvider = mockk<TimeProvider>()
    private lateinit var viewModel: StatisticsViewModel

    @BeforeEach
    fun setup() {
        every { timeProvider.currentMonth() } returns YearMonth.of(2026, 8)
        every { getStatisticsUseCase() } returns flowOf(TestDataFactory.createStatisticsData())
        viewModel = createViewModel()
    }

    private fun createViewModel() = StatisticsViewModel(
        getStatisticsUseCase,
        getAttendanceReportUseCase,
        pdfExporter,
        csvExporter,
        excelExporter,
        timeProvider
    )

    @Test
    fun `statistics should reflect use case flow`() = runTest {
        val statsData = TestDataFactory.createStatisticsData()
        every { getStatisticsUseCase() } returns flowOf(statsData)

        viewModel = createViewModel()

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
            selectedYear = 2026,
            monthlySummaries = listOf(TestDataFactory.createMonthlySummary()),
            averageAchieved = 50f,
            totalPresencial = 10,
            totalHomeOffice = 10,
            longestStreak = 5,
            currentStreak = 2,
            weeklySummaries = emptyList(),
            annualSummary = TestDataFactory.createAnnualSummary()
        )
        every { getStatisticsUseCase() } returns flowOf(statsData)
        viewModel = createViewModel()

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
        every { getStatisticsUseCase() } returns flowOf(statsData)
        viewModel = createViewModel()

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

    @Test
    fun `exportFileBaseName should use current month from time provider`() {
        assertEquals("presencial_2026-08", viewModel.exportFileBaseName())
    }

    @Test
    fun `exportCsv should call csvExporter with attendance report`() = runTest {
        val report = sampleReport()
        every { getAttendanceReportUseCase(YearMonth.of(2026, 8)) } returns flowOf(report)
        val outputStream = mockk<OutputStream>()
        every { csvExporter.export(report, outputStream) } returns Result.success(Unit)

        val result = viewModel.exportCsv(outputStream)

        assertTrue(result.isSuccess)
        verify { csvExporter.export(report, outputStream) }
    }

    @Test
    fun `exportExcel should call excelExporter with attendance report`() = runTest {
        val report = sampleReport()
        every { getAttendanceReportUseCase(YearMonth.of(2026, 8)) } returns flowOf(report)
        val outputStream = mockk<OutputStream>()
        every { excelExporter.export(report, outputStream) } returns Result.success(Unit)

        val result = viewModel.exportExcel(outputStream)

        assertTrue(result.isSuccess)
        verify { excelExporter.export(report, outputStream) }
    }

    private fun sampleReport() = AttendanceReport(
        yearMonth = YearMonth.of(2026, 8),
        rows = listOf(
            AttendanceReportRow(
                date = LocalDate.of(2026, 8, 3),
                dayOfWeekLabel = "domingo",
                statusLabel = "Presencial",
                isHoliday = false,
                isWorkday = true,
                holidayName = null
            )
        ),
        footer = AttendanceReportFooter(
            workdays = 21,
            requiredDays = 7,
            completedDays = 5,
            requiredPercentage = 40,
            achievedPercentage = 71.4f,
            exportedAt = LocalDate.of(2026, 8, 12)
        )
    )
}
