package com.presencial.app.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presencial.app.data.export.PdfExporter
import com.presencial.app.domain.usecase.GetStatisticsUseCase
import com.presencial.app.domain.usecase.StatisticsData
import com.presencial.app.domain.util.TimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getStatisticsUseCase: GetStatisticsUseCase,
    private val pdfExporter: PdfExporter,
    timeProvider: TimeProvider
) : ViewModel() {

    private val selectedYear = MutableStateFlow(timeProvider.currentMonth().year)

    val statistics: StateFlow<StatisticsData?> = selectedYear
        .flatMapLatest { year -> getStatisticsUseCase(year) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    fun previousYear() {
        selectedYear.update { it - 1 }
    }

    fun nextYear() {
        selectedYear.update { it + 1 }
    }

    fun exportPdf(outputStream: java.io.OutputStream): Result<Unit> {
        val data = statistics.value ?: return Result.failure(IllegalStateException("Sem dados"))
        return pdfExporter.exportStatistics(
            outputStream = outputStream,
            summaries = data.monthlySummaries,
            averageAchieved = data.averageAchieved,
            totalPresencial = data.totalPresencial,
            totalHomeOffice = data.totalHomeOffice
        )
    }
}

private const val STOP_TIMEOUT_MS = 5000L
