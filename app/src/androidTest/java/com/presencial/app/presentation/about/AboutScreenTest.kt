package com.presencial.app.presentation.about

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.presencial.app.ui.theme.PresencialTheme
import org.junit.Rule
import org.junit.Test

class AboutScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun aboutScreen_displaysAllInitialContent() {
        composeTestRule.setContent {
            PresencialTheme {
                AboutScreen(onBack = {})
            }
        }

        // Check TopAppBar title
        composeTestRule.onNodeWithText("Sobre").assertIsDisplayed()

        // Check Header content
        composeTestRule.onNodeWithText("Presencial").assertIsDisplayed()
        composeTestRule.onNodeWithText("Versão", substring = true).assertIsDisplayed()

        // Check Privacy Policy Card
        composeTestRule.onNodeWithText("Política de Privacidade").assertIsDisplayed()
        composeTestRule.onNodeWithText("Por padrão, todos os dados permanecem no dispositivo", substring = true).assertIsDisplayed()

        // Check Developer Card
        composeTestRule.onNodeWithText("Desenvolvido por").assertIsDisplayed()
        composeTestRule.onNodeWithText("Márcia Cristina").assertIsDisplayed()
        composeTestRule.onNodeWithText("GitHub").assertIsDisplayed()
    }

    @Test
    fun clickingBack_triggersOnBack() {
        var backClicked = false
        composeTestRule.setContent {
            PresencialTheme {
                AboutScreen(onBack = { backClicked = true })
            }
        }

        composeTestRule.onNodeWithContentDescription("Voltar").performClick()

        assert(backClicked)
    }
}
