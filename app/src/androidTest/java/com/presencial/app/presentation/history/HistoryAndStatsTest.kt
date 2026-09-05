package com.presencial.app.presentation.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.presencial.app.domain.model.HistoryMonthData
import com.presencial.app.domain.model.MonthlySummary
import com.presencial.app.domain.usecase.StatisticsData
import com.presencial.app.presentation.statistics.StatisticsScreen
import com.presencial.app.presentation.statistics.StatisticsViewModel
import com.presencial.app.ui.theme.PresencialTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth

class HistoryAndStatsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val historyViewModel = mockk<HistoryViewModel>(relaxed = true)
    private val statisticsViewModel = mockk<StatisticsViewModel>(relaxed = true)

    @Before
    fun setup() {
        every { statisticsViewModel.exportFileBaseName() } returns "presencial_2026-08"
    }

    @Test
    fun historyScreen_displaysSummaries() {
        val historyMonths = listOf(
            HistoryMonthData(
                summary = MonthlySummary(
                    yearMonth = YearMonth.of(2026, 8),
                    workdays = 21,
                    requiredDays = 12,
                    completedDays = 10,
                    homeOfficeDays = 0,
                    requiredPercentage = 60,
                    achievedPercentage = 83.3f
                ),
                autoCheckInDays = 3
            )
        )
        val historyFlow = MutableStateFlow(HistoryUiState.Ready(historyMonths))
        every { historyViewModel.uiState } returns historyFlow

        composeTestRule.setContent {
            PresencialTheme {
                HistoryScreen(viewModel = historyViewModel)
            }
        }

        // Verifica se "Agosto 2026" está visível (Formatado pelo HistoryMonthCard)
        composeTestRule.onNodeWithText("Agosto 2026").assertIsDisplayed()
    }

    @Test
    fun historyScreen_statisticsButton_invokesNavigation() {
        every { historyViewModel.uiState } returns MutableStateFlow(HistoryUiState.Ready(emptyList()))
        var openedStatistics = false

        composeTestRule.setContent {
            PresencialTheme {
                HistoryScreen(
                    viewModel = historyViewModel,
                    onNavigateToStatistics = { openedStatistics = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Ver estatísticas").performClick()
        assertTrue(openedStatistics)
    }

    @Test
    fun historyScreen_emptyState_showsMessageInsteadOfSkeleton() {
        every { historyViewModel.uiState } returns MutableStateFlow(HistoryUiState.Ready(emptyList()))

        composeTestRule.setContent {
            PresencialTheme {
                HistoryScreen(viewModel = historyViewModel)
            }
        }

        composeTestRule.onNodeWithText("Nenhum mês registrado ainda").assertIsDisplayed()
    }

    @Test
    fun statisticsScreen_displaysStatsAndChart() {
        val statsData = StatisticsData(
            selectedYear = 2026,
            monthlySummaries = emptyList(),
            averageAchieved = 85.5f,
            totalPresencial = 150,
            totalHomeOffice = 20,
            longestStreak = 15,
            currentStreak = 5,
            weeklySummaries = emptyList(),
            annualSummary = com.presencial.app.domain.model.AnnualSummary(
                year = 2026,
                averageAchieved = 85.5f,
                totalWorkdays = 220,
                totalPresencial = 150,
                goalsMetCount = 10,
                totalMonthsWithData = 12,
                bestMonth = null,
                worstMonth = null
            )
        )
        val statsFlow = MutableStateFlow<StatisticsData?>(statsData)
        every { statisticsViewModel.statistics } returns statsFlow

        composeTestRule.setContent {
            PresencialTheme {
                StatisticsScreen(viewModel = statisticsViewModel)
            }
        }

        composeTestRule.onNodeWithText("Estatísticas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Média anual").assertIsDisplayed()

        val averageLabel = "${"%.1f".format(85.5f)}%"
        composeTestRule.onAllNodesWithText(averageLabel, substring = true)[0]
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("5 dias").assertIsDisplayed()

        composeTestRule.onNodeWithText("Exportar PDF", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }
}
