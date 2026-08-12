package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.model.WeeklyPolicySummary
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.PresencePolicyCalculator
import com.presencial.app.domain.util.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.YearMonth
import javax.inject.Inject

class GetWeeklyPolicySummaryUseCase @Inject constructor(
    private val absenceRepository: AbsenceRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider
) {
    operator fun invoke(
        yearMonth: YearMonth = timeProvider.currentMonth()
    ): Flow<List<WeeklyPolicySummary>> {
        return combine(
            absenceRepository.getAbsencesInRange(yearMonth.atDay(1), yearMonth.atEndOfMonth()),
            settingsRepository.settings
        ) { absences, settings ->
            PresencePolicyCalculator.buildWeeklySummaries(
                yearMonth = yearMonth,
                countSaturdays = settings.countSaturdaysAsWorkdays,
                absences = absences,
                policy = settings.presencePolicy
            )
        }
    }
}
