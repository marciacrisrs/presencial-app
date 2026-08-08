package com.presencial.app.presentation.absence

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.presencial.app.domain.model.Absence
import com.presencial.app.domain.model.AbsenceType
import com.presencial.app.ui.theme.PresencialTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class AbsenceScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel: AbsenceViewModel = mockk(relaxed = true)
    private val absencesFlow = MutableStateFlow<List<Absence>>(emptyList())
    private val messageFlow = MutableStateFlow<String?>(null)

    init {
        every { viewModel.absences } returns absencesFlow
        every { viewModel.message } returns messageFlow
    }

    @Test
    fun initialEmptyState_isDisplayed() {
        absencesFlow.value = emptyList()

        composeTestRule.setContent {
            PresencialTheme {
                AbsenceScreen(viewModel = viewModel, onBack = {})
            }
        }

        composeTestRule.onNodeWithText("Nenhuma ausência registrada").assertIsDisplayed()
    }

    @Test
    fun listDisplay_showsItems() {
        val absences = listOf(
            Absence(id = 1, type = AbsenceType.VACATION, startDate = LocalDate.now(), endDate = LocalDate.now(), isFullDay = true)
        )
        absencesFlow.value = absences

        composeTestRule.setContent {
            PresencialTheme {
                AbsenceScreen(viewModel = viewModel, onBack = {})
            }
        }

        // Searching for displayName "Férias" which is mapped from VACATION
        composeTestRule.onNodeWithText("Férias").assertIsDisplayed()
    }

    @Test
    fun fabAction_opensAddDialog() {
        composeTestRule.setContent {
            PresencialTheme {
                AbsenceScreen(viewModel = viewModel, onBack = {})
            }
        }

        composeTestRule.onNodeWithContentDescription("Adicionar Ausência").performClick()
        composeTestRule.onNodeWithText("Registrar Ausência").assertIsDisplayed()
    }

    @Test
    fun deleteAction_callsViewModelDelete() {
        val absence = Absence(id = 1, type = AbsenceType.VACATION, startDate = LocalDate.now(), endDate = LocalDate.now(), isFullDay = true)
        absencesFlow.value = listOf(absence)

        composeTestRule.setContent {
            PresencialTheme {
                AbsenceScreen(viewModel = viewModel, onBack = {})
            }
        }

        composeTestRule.onNodeWithContentDescription("Remover").performClick()
        verify { viewModel.deleteAbsence(1) }
    }

    @Test
    fun addAction_callsViewModelAdd() {
        composeTestRule.setContent {
            PresencialTheme {
                AbsenceScreen(viewModel = viewModel, onBack = {})
            }
        }

        // Open Dialog
        composeTestRule.onNodeWithContentDescription("Adicionar Ausência").performClick()
        
        // Click save - using default values in dialog
        composeTestRule.onNodeWithText("Salvar Registro").performClick()

        // Verify that viewModel.addAbsence was called. 
        // We use any() for dates as they are LocalDate.now() in the dialog
        verify { viewModel.addAbsence(AbsenceType.VACATION, any(), any(), notes = null) }
    }

    @Test
    fun snackbar_isShownWhenMessagePresent() {
        composeTestRule.setContent {
            PresencialTheme {
                AbsenceScreen(viewModel = viewModel, onBack = {})
            }
        }

        val testMessage = "Mensagem de teste"
        messageFlow.value = testMessage
        
        composeTestRule.onNodeWithText(testMessage).assertIsDisplayed()
    }

    @Test
    fun backNavigation_callsOnBack() {
        var backClicked = false
        composeTestRule.setContent {
            PresencialTheme {
                AbsenceScreen(viewModel = viewModel, onBack = { backClicked = true })
            }
        }

        composeTestRule.onNodeWithContentDescription("Voltar").performClick()
        assert(backClicked)
    }
}
