package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.MonthlySummaryRepository
import java.time.LocalDate
import javax.inject.Inject

class ToggleTodayCheckInUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val monthlySummaryRepository: MonthlySummaryRepository
) {
    suspend operator fun invoke(date: LocalDate = LocalDate.now(), markPresencial: Boolean) {
        if (markPresencial) {
            checkInRepository.saveCheckIn(date, DayStatus.PRESENCIAL)
        } else {
            checkInRepository.deleteCheckIn(date)
        }
        monthlySummaryRepository.refreshSummary(
            java.time.YearMonth.from(date)
        )
    }
}
