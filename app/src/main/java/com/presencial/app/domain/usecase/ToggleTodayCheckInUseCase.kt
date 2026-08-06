package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.MonthlySummaryRepository
import com.presencial.app.domain.util.TimeProvider
import java.time.LocalDate
import javax.inject.Inject

class ToggleTodayCheckInUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val monthlySummaryRepository: MonthlySummaryRepository,
    private val timeProvider: TimeProvider
) {
    suspend operator fun invoke(date: LocalDate? = null, markPresencial: Boolean, source: String = "MANUAL") {
        val targetDate = date ?: timeProvider.today()
        if (markPresencial) {
            checkInRepository.saveCheckIn(targetDate, DayStatus.PRESENCIAL, source)
        } else {
            checkInRepository.deleteCheckIn(targetDate)
        }
        monthlySummaryRepository.refreshSummary(
            java.time.YearMonth.from(targetDate)
        )
    }
}
