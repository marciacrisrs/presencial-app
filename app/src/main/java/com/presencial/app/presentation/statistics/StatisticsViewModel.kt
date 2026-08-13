package com.presencial.app.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presencial.app.data.export.CsvExporter
import com.presencial.app.data.export.ExcelExporter
import com.presencial.app.data.export.PdfExporter
import com.presencial.app.domain.usecase.GetAttendanceReportUseCase
import com.presencial.app.domain.usecase.GetStatisticsUseCase
import com.presencial.app.domain.usecase.StatisticsData
import com.presencial.app.domain.util.TimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getStatisticsUseCase: GetStatisticsUseCase,
    private val getAttendanceReportUseCase: GetAttendanceReportUseCase,
    private val pdfExporter: PdfExporter,
    private val csvExporter: CsvExporter,
    private val excelExporter: ExcelExporter,
    private val timeProvider: TimeProvider
) : ViewModel() {

    val statistics: StateFlow<StatisticsData?> = getStatisticsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    fun exportPdf(outputStream: OutputStream): Result<Unit> {
        val data = statistics.value ?: return Result.failure(IllegalStateException("Sem dados"))
        return pdfExporter.exportStatistics(
            outputStream = outputStream,
            summaries = data.monthlySummaries,
            averageAchieved = data.averageAchieved,
            totalPresencial = data.totalPresencial,
            totalHomeOffice = data.totalHomeOffice
        )
    }

    suspend fun exportCsv(outputStream: OutputStream): Result<Unit> = withContext(Dispatchers.Default) {
        val report = getAttendanceReportUseCase(timeProvider.currentMonth()).first()
        csvExporter.export(report, outputStream)
    }

    suspend fun exportExcel(outputStream: OutputStream): Result<Unit> = withContext(Dispatchers.Default) {
        val report = getAttendanceReportUseCase(timeProvider.currentMonth()).first()
        excelExporter.export(report, outputStream)
    }

    fun exportFileBaseName(): String = exportFileBaseName(timeProvider.currentMonth())

    internal fun exportFileBaseName(yearMonth: YearMonth): String {
        val monthKey = "${yearMonth.year}-${yearMonth.monthValue.toString().padStart(2, '0')}"
        return "presencial_$monthKey"
    }
}

private const val STOP_TIMEOUT_MS = 5000L
