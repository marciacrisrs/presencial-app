package com.presencial.app.presentation.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.presencial.app.ui.theme.PresencialTheme
import org.junit.Rule
import org.junit.Test

class OnboardingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun goalStep_showsPercentagePresets() {
        composeTestRule.setContent {
            PresencialTheme {
                OnboardingContent(
                    step = 0,
                    selectedPercentage = 40,
                    onSelectPercentage = {},
                    onContinueGoal = {},
                    onContinueReminder = {},
                    onAddWorkAddress = {},
                    onSkipLocation = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Qual é a sua meta de presença?").assertIsDisplayed()
        composeTestRule.onNodeWithText("40%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continuar").assertIsDisplayed()
    }

    @Test
    fun locationStep_allowsSkipping() {
        var skipped = false
        composeTestRule.setContent {
            PresencialTheme {
                OnboardingContent(
                    step = 2,
                    selectedPercentage = 40,
                    onSelectPercentage = {},
                    onContinueGoal = {},
                    onContinueReminder = {},
                    onAddWorkAddress = {},
                    onSkipLocation = { skipped = true }
                )
            }
        }
        composeTestRule.onNodeWithText("Onde fica o escritório?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Concluir").performClick()
        org.junit.Assert.assertTrue(skipped)
    }
}
