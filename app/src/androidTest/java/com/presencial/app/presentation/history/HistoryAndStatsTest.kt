package com.presencial.app.presentation.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.presencial.app.domain.model.HistoryMonthData
import com.presencial.app.domain.model.MonthlySummary
import com.presencial.app.domain.usecase.StatisticsData
import com.presencial.app.presentation.statistics.StatisticsScreen
import com.presencial.app.presentation.statistics.StatisticsViewModel
import com.presencial.app.ui.theme.PresencialTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth

class HistoryAndStatsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val historyViewModel = mockk<HistoryViewModel>(relaxed = true)
    private val statisticsViewModel = mockk<StatisticsViewModel>(relaxed = true)

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
        val historyFlow = MutableStateFlow(historyMonths)
        every { historyViewModel.historyMonths } returns historyFlow

        composeTestRule.setContent {
            PresencialTheme {
                HistoryScreen(viewModel = historyViewModel)
            }
        }

        // Verifica se "Agosto 2026" está visível (Formatado pelo HistoryMonthCard)
        composeTestRule.onNodeWithText("Agosto 2026").assertIsDisplayed()
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
            ),
            heatmapDays = emptyList()
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
        
        // Verificando valores (pode variar o formato decimal conforme o locale do teste)
        // Tentamos encontrar o valor da média e a sequência atual
        composeTestRule.onNodeWithText("85,5%", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("5 dias").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Exportar PDF").assertIsDisplayed()
    }
}
