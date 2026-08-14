package com.presencial.app.presentation.absence

import app.cash.turbine.test
import com.presencial.app.domain.model.Absence
import com.presencial.app.domain.model.AbsenceType
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.widget.WidgetRefresher
import com.presencial.app.util.MainDispatcherExtension
import com.presencial.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.LocalDate

class AbsenceViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val repository = mockk<AbsenceRepository>()
    private val widgetRefresher = mockk<WidgetRefresher>(relaxed = true)
    private lateinit var viewModel: AbsenceViewModel

    @BeforeEach
    fun setup() {
        every { repository.getAllAbsences() } returns flowOf(emptyList())
        coEvery { widgetRefresher.refresh() } returns Unit
        viewModel = AbsenceViewModel(repository, widgetRefresher)
    }

    @Test
    fun `absences should reflect repository flow`() = runTest {
        val absenceList = listOf(TestDataFactory.createAbsence())
        every { repository.getAllAbsences() } returns flowOf(absenceList)
        
        viewModel = AbsenceViewModel(repository, widgetRefresher)

        viewModel.absences.test {
            assertEquals(absenceList, awaitItem())
        }
    }

    @Test
    fun `addAbsence should show error if endDate is before startDate`() = runTest {
        val start = LocalDate.of(2026, 8, 10)
        val end = LocalDate.of(2026, 8, 5)

        viewModel.addAbsence(AbsenceType.VACATION, start, end)

        assertEquals("A data final não pode ser anterior à data inicial", viewModel.message.value)
    }

    @Test
    fun `addAbsence should call repository and show success message`() = runTest {
        val start = LocalDate.of(2026, 8, 1)
        val end = LocalDate.of(2026, 8, 5)
        
        coEvery { repository.insertAbsence(any()) } returns Unit

        viewModel.addAbsence(AbsenceType.VACATION, start, end)

        coVerify { repository.insertAbsence(match { 
            it.type == AbsenceType.VACATION && 
            it.startDate == start && 
            it.endDate == end &&
            it.isFullDay &&
            it.hours == 8f &&
            it.notes == null &&
            !it.isCounted
        }) }
        assertEquals("Registro adicionado com sucesso", viewModel.message.value)
        coVerify { widgetRefresher.refresh() }
    }

    @Test
    fun `addAbsence should succeed when startDate equals endDate`() = runTest {
        val date = LocalDate.of(2026, 8, 1)
        
        coEvery { repository.insertAbsence(any()) } returns Unit

        viewModel.addAbsence(AbsenceType.ABSENCE, date, date)

        coVerify { repository.insertAbsence(match { 
            it.startDate == date && it.endDate == date 
        }) }
        assertEquals("Registro adicionado com sucesso", viewModel.message.value)
    }

    @Test
    fun `addAbsence should correctly pass optional notes and other parameters`() = runTest {
        val start = LocalDate.of(2026, 8, 1)
        val end = LocalDate.of(2026, 8, 2)
        val notes = "Some important note"
        val hours = 4.5f
        val isFullDay = false
        
        coEvery { repository.insertAbsence(any()) } returns Unit

        viewModel.addAbsence(
            type = AbsenceType.DAY_OFF,
            startDate = start,
            endDate = end,
            isFullDay = isFullDay,
            hours = hours,
            notes = notes
        )

        coVerify { repository.insertAbsence(match { 
            it.type == AbsenceType.DAY_OFF && 
            it.startDate == start && 
            it.endDate == end &&
            it.isFullDay == isFullDay &&
            it.hours == hours &&
            it.notes == notes &&
            !it.isCounted
        }) }
        assertEquals("Registro adicionado com sucesso", viewModel.message.value)
    }

    @Test
    fun `deleteAbsence should call repository and show message`() = runTest {
        coEvery { repository.deleteById(1L) } returns Unit

        viewModel.deleteAbsence(1L)

        coVerify { repository.deleteById(1L) }
        coVerify { widgetRefresher.refresh() }
        assertEquals("Registro removido", viewModel.message.value)
    }

    @Test
    fun `clearMessage should reset message to null`() = runTest {
        coEvery { repository.deleteById(1L) } returns Unit
        viewModel.deleteAbsence(1L)
        
        viewModel.clearMessage()
        
        assertNull(viewModel.message.value)
    }
}
