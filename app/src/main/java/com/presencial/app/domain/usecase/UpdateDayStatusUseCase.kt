package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.MonthlySummaryRepository
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class UpdateDayStatusUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val monthlySummaryRepository: MonthlySummaryRepository
) {
    suspend operator fun invoke(date: LocalDate, status: DayStatus, source: String = "MANUAL") {
        when (status) {
            DayStatus.HOME_OFFICE, DayStatus.PRESENCIAL -> {
                checkInRepository.saveCheckIn(date, status, source)
            }
            else -> checkInRepository.deleteCheckIn(date)
        }
        monthlySummaryRepository.refreshSummary(YearMonth.from(date))
    }
}
