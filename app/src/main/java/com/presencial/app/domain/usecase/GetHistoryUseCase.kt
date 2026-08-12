package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.CheckInSource
import com.presencial.app.domain.model.HistoryMonthData
import com.presencial.app.domain.model.MonthlySummary
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.MonthlySummaryRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.PresencePolicyCalculator
import com.presencial.app.domain.util.TimeProvider
import com.presencial.app.domain.util.WorkdayCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.YearMonth
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val monthlySummaryRepository: MonthlySummaryRepository,
    private val checkInRepository: CheckInRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider
) {
    operator fun invoke(): Flow<List<HistoryMonthData>> {
        return combine(
            monthlySummaryRepository.observeAllSummaries(),
            checkInRepository.observeAllCheckIns(),
            settingsRepository.settings
        ) { summaries, checkIns, settings ->
            val enrichedSummaries = ensureCurrentMonth(summaries, settings)
            enrichedSummaries.map { summary ->
                val autoCount = checkIns.count { checkIn ->
                    checkIn.date.year == summary.yearMonth.year &&
                        checkIn.date.month == summary.yearMonth.month &&
                        CheckInSource.isAutoGeofence(checkIn.source)
                }
                HistoryMonthData(summary = summary, autoCheckInDays = autoCount)
            }
        }
    }

    private fun ensureCurrentMonth(
        summaries: List<MonthlySummary>,
        settings: com.presencial.app.domain.model.AppSettings
    ): List<MonthlySummary> {
        val current = timeProvider.currentMonth()
        if (summaries.any { it.yearMonth == current }) return summaries

        val workdays = WorkdayCalculator.countWorkdaysInMonth(
            current,
            settings.countSaturdaysAsWorkdays
        )
        val required = PresencePolicyCalculator.calculateRequiredDays(
            current,
            settings.countSaturdaysAsWorkdays,
            emptyList(),
            settings.presencePolicy
        )
        return summaries + MonthlySummary(
            yearMonth = current,
            workdays = workdays,
            requiredDays = required,
            completedDays = 0,
            homeOfficeDays = 0,
            requiredPercentage = settings.requiredPercentage,
            achievedPercentage = 0f
        )
    }
}
