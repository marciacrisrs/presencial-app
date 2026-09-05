package com.presencial.app.presentation.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.presencial.app.domain.model.DashboardData
import com.presencial.app.ui.theme.PresencialTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth

class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel: DashboardViewModel = mockk(relaxed = true)
    private val dashboardData = MutableStateFlow<DashboardData?>(null)
    private val workAddresses = MutableStateFlow(emptyList<com.presencial.app.domain.model.WorkAddress>())

    init {
        every { viewModel.dashboardData } returns dashboardData
        every { viewModel.workAddresses } returns workAddresses
        every { viewModel.uiEvents } returns emptyFlow()
    }

    @Test
    fun dashboardScreen_initialLoading_showsSkeleton() {
        dashboardData.value = null

        composeTestRule.setContent {
            PresencialTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }

        // Verify that the month/year text is NOT displayed (indicating skeleton or empty state)
        // Using "Agosto 2026" as an example of content that should only be in the success state
        assertTrue(
            composeTestRule.onAllNodesWithText("Agosto 2026").fetchSemanticsNodes().isEmpty()
        )
    }

    @Test
    fun dashboardScreen_withData_showsContent() {
        dashboardData.value = createMockDashboardData()

        composeTestRule.setContent {
            PresencialTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }

        // Verify month/year is shown
        composeTestRule.onNodeWithText("Agosto 2026").assertIsDisplayed()
        composeTestRule.onNodeWithText("5 de 15").assertIsDisplayed()
        composeTestRule.onNodeWithText("Faltam 10 dias").assertIsDisplayed()
        
        // Verify main action is shown
        composeTestRule.onNodeWithText("Registrar presença hoje")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun clickingCheckIn_triggersViewModelAction() {
        dashboardData.value = createMockDashboardData()

        composeTestRule.setContent {
            PresencialTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Registrar presença hoje")
            .performScrollTo()
            .performClick()

        verify { viewModel.toggleTodayCheckIn(true) }
    }

    @Test
    fun clickingYesterdayRegister_triggersViewModelAction() {
        dashboardData.value = createMockDashboardData(yesterdayIsPending = true)

        composeTestRule.setContent {
            PresencialTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }

        // Click "Registrar" in the yesterday card
        composeTestRule.onNodeWithText("Registrar").performClick()

        verify { viewModel.markYesterdayPresencial() }
    }

    @Test
    fun openCheckIn_doesNotConsumeWhenHomeIsNotVisible() {
        dashboardData.value = createMockDashboardData()
        var handled = false

        composeTestRule.setContent {
            PresencialTheme {
                DashboardScreen(
                    viewModel = viewModel,
                    openCheckIn = true,
                    isHomeVisible = false,
                    onCheckInHandled = { handled = true }
                )
            }
        }

        composeTestRule.waitForIdle()
        org.junit.Assert.assertFalse(handled)
    }

    @Test
    fun openCheckIn_consumesWhenHomeIsVisible() {
        dashboardData.value = createMockDashboardData()
        var handled = false

        composeTestRule.setContent {
            PresencialTheme {
                DashboardScreen(
                    viewModel = viewModel,
                    openCheckIn = true,
                    isHomeVisible = true,
                    onCheckInHandled = { handled = true }
                )
            }
        }

        composeTestRule.waitForIdle()
        assertTrue(handled)
    }

    private fun createMockDashboardData(
        yesterdayIsPending: Boolean = false
    ) = DashboardData(
        yearMonth = YearMonth.of(2026, 8),
        totalDays = 31,
        workdays = 22,
        requiredDays = 15,
        completedDays = 5,
        remainingDays = 10,
        homeOfficeDays = 5,
        achievedPercentage = 33.3f,
        requiredPercentage = 60,
        progressFraction = 0.33f,
        smartMessage = "Bom trabalho!",
        countSaturdays = false,
        todayIsPresencial = false,
        todayIsWorkday = true,
        yesterdayIsPending = yesterdayIsPending,
        streak = 3
    )
}
