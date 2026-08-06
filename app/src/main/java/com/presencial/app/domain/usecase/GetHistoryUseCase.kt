package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.MonthlySummary
import com.presencial.app.domain.repository.MonthlySummaryRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.GoalCalculator
import com.presencial.app.domain.util.TimeProvider
import com.presencial.app.domain.util.WorkdayCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.YearMonth
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val monthlySummaryRepository: MonthlySummaryRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider
) {
    operator fun invoke(): Flow<List<MonthlySummary>> {
        return combine(
            monthlySummaryRepository.observeAllSummaries(),
            settingsRepository.settings
        ) { summaries, settings ->
            val current = timeProvider.currentMonth()
            if (summaries.none { it.yearMonth == current }) {
                val workdays = WorkdayCalculator.countWorkdaysInMonth(current, settings.countSaturdaysAsWorkdays)
                val required = GoalCalculator.calculateRequiredDays(workdays, settings.requiredPercentage)
                summaries + MonthlySummary(
                    yearMonth = current,
                    workdays = workdays,
                    requiredDays = required,
                    completedDays = 0,
                    homeOfficeDays = 0,
                    requiredPercentage = settings.requiredPercentage,
                    achievedPercentage = 0f
                )
            } else summaries
        }
    }
}
