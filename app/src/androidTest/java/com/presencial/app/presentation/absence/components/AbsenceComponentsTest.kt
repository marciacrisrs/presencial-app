package com.presencial.app.presentation.absence.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.presencial.app.domain.model.Absence
import com.presencial.app.domain.model.AbsenceType
import com.presencial.app.ui.theme.PresencialTheme
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class AbsenceComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun absenceItem_displaysCorrectInformation() {
        val absence = Absence(
            type = AbsenceType.VACATION,
            startDate = LocalDate.of(2026, 8, 10),
            endDate = LocalDate.of(2026, 8, 20),
            isFullDay = true,
            notes = "Férias de verão",
        )

        composeTestRule.setContent {
            PresencialTheme {
                AbsenceItem(absence = absence) {}
            }
        }

        composeTestRule.onNodeWithText("Férias").assertIsDisplayed()
        composeTestRule.onNodeWithText("10/08/2026 - 20/08/2026").assertIsDisplayed()
        composeTestRule.onNodeWithText("Férias de verão").assertIsDisplayed()
    }

    @Test
    fun absenceItem_displaysSingleDatePeriod() {
        val absence = Absence(
            type = AbsenceType.DAY_OFF,
            startDate = LocalDate.of(2026, 8, 10),
            endDate = LocalDate.of(2026, 8, 10),
            isFullDay = true,
        )

        composeTestRule.setContent {
            PresencialTheme {
                AbsenceItem(absence = absence) {}
            }
        }

        composeTestRule.onNodeWithText("10/08/2026").assertIsDisplayed()
    }

    @Test
    fun absenceItem_onDeleteTriggered() {
        var deleteClicked = false
        val absence = Absence(
            type = AbsenceType.ABSENCE,
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
            isFullDay = true,
        )

        composeTestRule.setContent {
            PresencialTheme {
                AbsenceItem(
                    absence = absence,
                    onDelete = { deleteClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Remover").performClick()
        assert(deleteClicked)
    }

    @Test
    fun addAbsenceDialog_displaysInitialContent() {
        composeTestRule.setContent {
            PresencialTheme {
                AddAbsenceDialog(
                    onDismiss = {},
                    onConfirm = { _, _, _, _ -> },
                )
            }
        }

        composeTestRule.onNodeWithText("Registrar Ausência").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tipo de ausência").assertIsDisplayed()
        composeTestRule.onNodeWithText("Início").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fim").assertIsDisplayed()
        composeTestRule.onNodeWithText("Observações (opcional)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Salvar Registro").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").assertIsDisplayed()
    }

    @Test
    fun addAbsenceDialog_onDismissTriggered() {
        var dismissClicked = false
        composeTestRule.setContent {
            PresencialTheme {
                AddAbsenceDialog(
                    onDismiss = { dismissClicked = true },
                    onConfirm = { _, _, _, _ -> },
                )
            }
        }

        composeTestRule.onNodeWithText("Cancelar").performClick()
        assert(dismissClicked)
    }

    @Test
    fun addAbsenceDialog_onConfirmTriggeredWithCorrectData() {
        var confirmedType: AbsenceType? = null
        var confirmedNotes: String? = null

        composeTestRule.setContent {
            PresencialTheme {
                AddAbsenceDialog(
                    onDismiss = {},
                    onConfirm = { type, _, _, notes ->
                        confirmedType = type
                        confirmedNotes = notes
                    },
                )
            }
        }

        // Change type to DAY_OFF (display name is "Day off")
        composeTestRule.onNodeWithText("Day off", substring = true).performClick()

        // Enter notes
        composeTestRule.onNodeWithText("Observações (opcional)").performTextInput("Compensação")

        // Confirm
        composeTestRule.onNodeWithText("Salvar Registro").performClick()

        assert(confirmedType == AbsenceType.DAY_OFF)
        assert(confirmedNotes == "Compensação")
    }
}
