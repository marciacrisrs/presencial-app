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
                    onFinish = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Boas-vindas ao Presencial").assertIsDisplayed()
        composeTestRule.onNodeWithText("Qual é a sua meta de presença?").assertIsDisplayed()
        composeTestRule.onNodeWithText("40%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continuar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Concluir").assertIsDisplayed()
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
                    onFinish = { skipped = true }
                )
            }
        }
        composeTestRule.onNodeWithText("Onde fica o escritório?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Concluir").performClick()
        org.junit.Assert.assertTrue(skipped)
    }

    @Test
    fun reminderStep_showsEighteenHourCopy() {
        composeTestRule.setContent {
            PresencialTheme {
                OnboardingContent(
                    step = 1,
                    selectedPercentage = 40,
                    onSelectPercentage = {},
                    onContinueGoal = {},
                    onContinueReminder = {},
                    onAddWorkAddress = {},
                    onFinish = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Lembrete às 18h").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continuar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Concluir").assertIsDisplayed()
    }

    @Test
    fun goalStep_finishCompletesOnboarding() {
        var finished = false
        composeTestRule.setContent {
            PresencialTheme {
                OnboardingContent(
                    step = 0,
                    selectedPercentage = 40,
                    onSelectPercentage = {},
                    onContinueGoal = {},
                    onContinueReminder = {},
                    onAddWorkAddress = {},
                    onFinish = { finished = true }
                )
            }
        }
        composeTestRule.onNodeWithText("Concluir").performClick()
        org.junit.Assert.assertTrue(finished)
    }

    @Test
    fun outOfRangeStep_isNormalizedToLocation() {
        composeTestRule.setContent {
            PresencialTheme {
                OnboardingContent(
                    step = 99,
                    selectedPercentage = 40,
                    onSelectPercentage = {},
                    onContinueGoal = {},
                    onContinueReminder = {},
                    onAddWorkAddress = {},
                    onFinish = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Onde fica o escritório?").assertIsDisplayed()
    }
}
