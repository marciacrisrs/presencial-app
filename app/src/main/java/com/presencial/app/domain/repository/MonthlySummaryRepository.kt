package com.presencial.app.domain.repository

import com.presencial.app.domain.model.MonthlySummary
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface MonthlySummaryRepository {
    fun observeAllSummaries(): Flow<List<MonthlySummary>>
    suspend fun getSummary(yearMonth: YearMonth): MonthlySummary?
    suspend fun saveSummary(summary: MonthlySummary)
    suspend fun refreshSummary(yearMonth: YearMonth)
}
