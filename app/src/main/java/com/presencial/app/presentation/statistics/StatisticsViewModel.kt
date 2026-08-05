package com.presencial.app.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presencial.app.data.export.PdfExporter
import com.presencial.app.domain.usecase.GetStatisticsUseCase
import com.presencial.app.domain.usecase.StatisticsData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.io.File
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    getStatisticsUseCase: GetStatisticsUseCase,
    private val pdfExporter: PdfExporter
) : ViewModel() {

    val statistics: StateFlow<StatisticsData?> = getStatisticsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun exportPdf(file: File): Result<Unit> {
        val data = statistics.value ?: return Result.failure(IllegalStateException("Sem dados"))
        return pdfExporter.exportStatistics(
            file = file,
            summaries = data.monthlySummaries,
            averageAchieved = data.averageAchieved,
            totalPresencial = data.totalPresencial,
            totalHomeOffice = data.totalHomeOffice
        )
    }
}
