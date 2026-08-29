package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.Absence
import com.presencial.app.domain.model.AbsenceType
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.MonthlySummaryRepository
import com.presencial.app.domain.widget.WidgetRefresher
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class UpdateDayStatusUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val absenceRepository: AbsenceRepository,
    private val monthlySummaryRepository: MonthlySummaryRepository,
    private val widgetRefresher: WidgetRefresher
) {
    suspend operator fun invoke(date: LocalDate, status: DayStatus, source: String = "MANUAL") {
        when (status) {
            DayStatus.HOME_OFFICE, DayStatus.PRESENCIAL -> {
                checkInRepository.saveCheckIn(date, status, source)
                removeSingleDayAbsence(date)
            }
            DayStatus.ABSENCE -> {
                checkInRepository.deleteCheckIn(date)
                ensureFullDayAbsence(date)
            }
            else -> {
                checkInRepository.deleteCheckIn(date)
                removeSingleDayAbsence(date)
            }
        }
        monthlySummaryRepository.refreshSummary(YearMonth.from(date))
        widgetRefresher.refresh()
    }

    private suspend fun coveringFullDayAbsences(date: LocalDate): List<Absence> =
        absenceRepository.getAbsencesInRange(date, date).first()
            .filter { absence ->
                absence.isFullDay &&
                    !date.isBefore(absence.startDate) &&
                    !date.isAfter(absence.endDate)
            }

    private suspend fun ensureFullDayAbsence(date: LocalDate) {
        if (coveringFullDayAbsences(date).isNotEmpty()) return
        absenceRepository.insertAbsence(
            Absence(
                type = AbsenceType.ABSENCE,
                startDate = date,
                endDate = date,
                isFullDay = true
            )
        )
    }

    private suspend fun removeSingleDayAbsence(date: LocalDate) {
        coveringFullDayAbsences(date)
            .filter { it.startDate == date && it.endDate == date }
            .forEach { absenceRepository.deleteById(it.id) }
    }
}
