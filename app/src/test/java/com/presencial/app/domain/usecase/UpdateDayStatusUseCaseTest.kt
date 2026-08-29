package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.AbsenceType
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.MonthlySummaryRepository
import com.presencial.app.domain.widget.WidgetRefresher
import com.presencial.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class UpdateDayStatusUseCaseTest {

    private val checkInRepository: CheckInRepository = mockk()
    private val absenceRepository: AbsenceRepository = mockk()
    private val monthlySummaryRepository: MonthlySummaryRepository = mockk()
    private val widgetRefresher: WidgetRefresher = mockk()
    private lateinit var useCase: UpdateDayStatusUseCase

    private val date = LocalDate.of(2026, 8, 6)

    @BeforeEach
    fun setup() {
        coEvery { checkInRepository.saveCheckIn(any(), any(), any()) } returns Unit
        coEvery { checkInRepository.deleteCheckIn(any()) } returns Unit
        coEvery { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(emptyList())
        coEvery { absenceRepository.insertAbsence(any()) } returns Unit
        coEvery { absenceRepository.deleteById(any()) } returns Unit
        coEvery { monthlySummaryRepository.refreshSummary(any()) } returns Unit
        coEvery { widgetRefresher.refresh() } returns Unit
        useCase = UpdateDayStatusUseCase(
            checkInRepository,
            absenceRepository,
            monthlySummaryRepository,
            widgetRefresher
        )
    }

    @Test
    fun `given presencial status, when invoke, then saveCheckIn is called`() = runTest {
        useCase(date, DayStatus.PRESENCIAL)

        coVerify { checkInRepository.saveCheckIn(date, DayStatus.PRESENCIAL, "MANUAL") }
        coVerify { monthlySummaryRepository.refreshSummary(YearMonth.of(2026, 8)) }
        coVerify { widgetRefresher.refresh() }
        coVerify(exactly = 0) { absenceRepository.insertAbsence(any()) }
    }

    @Test
    fun `given home office status, when invoke, then saveCheckIn is called`() = runTest {
        useCase(date, DayStatus.HOME_OFFICE)

        coVerify { checkInRepository.saveCheckIn(date, DayStatus.HOME_OFFICE, "MANUAL") }
        coVerify { monthlySummaryRepository.refreshSummary(YearMonth.of(2026, 8)) }
        coVerify { widgetRefresher.refresh() }
    }

    @Test
    fun `given absence status, when invoke, then delete check-in and insert full-day absence`() = runTest {
        useCase(date, DayStatus.ABSENCE)

        coVerify { checkInRepository.deleteCheckIn(date) }
        coVerify {
            absenceRepository.insertAbsence(
                match {
                    it.type == AbsenceType.ABSENCE &&
                        it.startDate == date &&
                        it.endDate == date &&
                        it.isFullDay &&
                        !it.isCounted
                }
            )
        }
        coVerify { monthlySummaryRepository.refreshSummary(YearMonth.of(2026, 8)) }
        coVerify { widgetRefresher.refresh() }
        coVerify(exactly = 0) { checkInRepository.saveCheckIn(any(), any(), any()) }
    }

    @Test
    fun `given absence status when day already has absence, when invoke, then do not insert duplicate`() =
        runTest {
            coEvery { absenceRepository.getAbsencesInRange(date, date) } returns flowOf(
                listOf(
                    TestDataFactory.createAbsence(
                        startDate = date,
                        endDate = date,
                        isFullDay = true
                    )
                )
            )

            useCase(date, DayStatus.ABSENCE)

            coVerify { checkInRepository.deleteCheckIn(date) }
            coVerify(exactly = 0) { absenceRepository.insertAbsence(any()) }
        }

    @Test
    fun `given presencial status with single-day absence, when invoke, then remove that absence`() =
        runTest {
            val singleDay = TestDataFactory.createAbsence(
                id = 9L,
                startDate = date,
                endDate = date,
                isFullDay = true
            )
            coEvery { absenceRepository.getAbsencesInRange(date, date) } returns flowOf(listOf(singleDay))

            useCase(date, DayStatus.PRESENCIAL)

            coVerify { checkInRepository.saveCheckIn(date, DayStatus.PRESENCIAL, "MANUAL") }
            coVerify { absenceRepository.deleteById(9L) }
        }

    @Test
    fun `given clear status, when invoke, then delete check-in and single-day absence only`() = runTest {
        val singleDay = TestDataFactory.createAbsence(
            id = 4L,
            startDate = date,
            endDate = date,
            isFullDay = true
        )
        val vacationRange = TestDataFactory.createAbsence(
            id = 5L,
            type = AbsenceType.VACATION,
            startDate = date.minusDays(1),
            endDate = date.plusDays(1),
            isFullDay = true
        )
        coEvery { absenceRepository.getAbsencesInRange(date, date) } returns flowOf(
            listOf(singleDay, vacationRange)
        )

        useCase(date, DayStatus.FERIADO)

        coVerify { checkInRepository.deleteCheckIn(date) }
        coVerify { absenceRepository.deleteById(4L) }
        coVerify(exactly = 0) { absenceRepository.deleteById(5L) }
        coVerify { monthlySummaryRepository.refreshSummary(YearMonth.of(2026, 8)) }
        coVerify { widgetRefresher.refresh() }
    }
}
