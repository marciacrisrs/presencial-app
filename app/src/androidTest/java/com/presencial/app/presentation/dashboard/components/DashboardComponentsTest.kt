package com.presencial.app.presentation.dashboard.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.presencial.app.domain.model.DashboardData
import com.presencial.app.ui.components.CircularProgressCard
import com.presencial.app.ui.components.DashboardProgressBar
import com.presencial.app.ui.components.SmartMessageCard
import com.presencial.app.ui.components.StatCard
import com.presencial.app.ui.theme.PresencialTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth

class DashboardComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockDashboardData = DashboardData(
        yearMonth = YearMonth.of(2026, 8),
        totalDays = 31,
        workdays = 22,
        requiredDays = 15,
        completedDays = 8,
        remainingDays = 7,
        homeOfficeDays = 14,
        achievedPercentage = 53f,
        requiredPercentage = 68,
        progressFraction = 0.53f,
        smartMessage = "Você está no caminho certo!",
        countSaturdays = false,
        todayIsPresencial = false,
        todayIsWorkday = true,
        yesterdayIsPending = true,
        streak = 3
    )

    @Test
    fun SmartMessageCard_displaysMessage() {
        val message = "Olá, mundo!"
        composeTestRule.setContent {
            PresencialTheme {
                SmartMessageCard(message = message)
            }
        }
        composeTestRule.onNodeWithText(message).assertIsDisplayed()
    }

    @Test
    fun StatCard_displaysAllInfo() {
        val title = "Título"
        val value = "10"
        val subtitle = "Subtítulo"
        composeTestRule.setContent {
            PresencialTheme {
                StatCard(title = title, value = value, subtitle = subtitle)
            }
        }
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(value).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }

    @Test
    fun CircularProgressCard_displaysLabelWithoutPercentage() {
        val label = "Meta: 60%"
        composeTestRule.setContent {
            PresencialTheme {
                CircularProgressCard(progress = 0.5f, label = label)
            }
        }
        composeTestRule.onNodeWithText(label).assertIsDisplayed()
        assertTrue(
            composeTestRule.onAllNodesWithText("50%").fetchSemanticsNodes().isEmpty()
        )
    }

    @Test
    fun DashboardProgressBar_displaysProgressText() {
        composeTestRule.setContent {
            PresencialTheme {
                DashboardProgressBar(data = mockDashboardData)
            }
        }
        composeTestRule.onNodeWithText("8 de 15 dias presenciais").assertIsDisplayed()
    }

    @Test
    fun CheckInButton_notRegistered_showsActionText() {
        composeTestRule.setContent {
            PresencialTheme {
                CheckInButton(isPresencial = false, onConfirm = {})
            }
        }
        composeTestRule.onNodeWithText("Registrar presença hoje").assertIsDisplayed()
    }

    @Test
    fun CheckInButton_registered_showsSuccessText() {
        composeTestRule.setContent {
            PresencialTheme {
                CheckInButton(isPresencial = true, onConfirm = {})
            }
        }
        composeTestRule.onNodeWithText("Presença registrada").assertIsDisplayed()
    }

    @Test
    fun YesterdayCheckInCard_displaysMessageAndButton() {
        composeTestRule.setContent {
            PresencialTheme {
                YesterdayCheckInCard(onConfirm = {})
            }
        }
        composeTestRule.onNodeWithText("Esqueceu de ontem?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Registrar").assertIsDisplayed()
    }

    @Test
    fun DashboardHeader_displaysMonthAndYear() {
        composeTestRule.setContent {
            PresencialTheme {
                DashboardHeader(dashboard = mockDashboardData)
            }
        }
        // YearMonth.of(2026, 8) -> "Agosto 2026" (pt-BR)
        composeTestRule.onNodeWithText("Agosto 2026").assertIsDisplayed()
    }
}
