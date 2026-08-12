package com.presencial.app.data.repository

import com.presencial.app.data.local.dao.CheckInDao
import com.presencial.app.data.local.dao.MonthlySummaryDao
import com.presencial.app.data.local.mapper.toDomain
import com.presencial.app.data.local.mapper.toEntity
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.model.MonthlySummary
import com.presencial.app.domain.repository.MonthlySummaryRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.GoalCalculator
import com.presencial.app.domain.util.PresencePolicyCalculator
import com.presencial.app.domain.util.WorkdayCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonthlySummaryRepositoryImpl @Inject constructor(
    private val monthlySummaryDao: MonthlySummaryDao,
    private val checkInDao: CheckInDao,
    private val settingsRepository: SettingsRepository
) : MonthlySummaryRepository {

    override fun observeAllSummaries(): Flow<List<MonthlySummary>> =
        monthlySummaryDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getSummary(yearMonth: YearMonth): MonthlySummary? =
        monthlySummaryDao.getByKey(yearMonth.toString())?.toDomain()

    override suspend fun saveSummary(summary: MonthlySummary) {
        monthlySummaryDao.upsert(summary.toEntity())
    }

    override suspend fun refreshSummary(yearMonth: YearMonth) {
        val settings = settingsRepository.settings.first()
        val start = yearMonth.atDay(1).toEpochDay()
        val end = yearMonth.atEndOfMonth().toEpochDay()
        val checkIns = checkInDao.getBetween(start, end)
        val workdays = WorkdayCalculator.countWorkdaysInMonth(yearMonth, settings.countSaturdaysAsWorkdays)
        val required = PresencePolicyCalculator.calculateRequiredDays(
            yearMonth,
            settings.countSaturdaysAsWorkdays,
            emptyList(),
            settings.presencePolicy
        )
        val completed = checkIns.count { it.status == DayStatus.PRESENCIAL.name }
        val homeOffice = checkIns.count { it.status == DayStatus.HOME_OFFICE.name }

        saveSummary(
            MonthlySummary(
                yearMonth = yearMonth,
                workdays = workdays,
                requiredDays = required,
                completedDays = completed,
                homeOfficeDays = homeOffice,
                requiredPercentage = settings.requiredPercentage,
                achievedPercentage = GoalCalculator.calculateAchievedPercentage(completed, required)
            )
        )
    }
}
