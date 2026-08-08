package com.presencial.app.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.presencial.app.domain.model.MonthlySummary
import com.presencial.app.ui.theme.PresencialTheme
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth

class ChartComponentsTest {

    @get:Rule
    val composeTestRule: ComposeContentTestRule = createComposeRule()

    @Test
    fun monthlyBarChart_emptyList_displaysEmptyMessage() {
        composeTestRule.setContent {
            PresencialTheme {
                MonthlyBarChart(summaries = emptyList())
            }
        }

        composeTestRule.onNodeWithText("Sem dados para exibir").assertIsDisplayed()
    }

    @Test
    fun monthlyBarChart_withData_displaysTitleAndSummaryText() {
        val summary = MonthlySummary(
            yearMonth = YearMonth.of(2024, 1),
            workdays = 22,
            requiredDays = 10,
            completedDays = 5,
            homeOfficeDays = 12,
            requiredPercentage = 45,
            achievedPercentage = 50f,
        )

        composeTestRule.setContent {
            PresencialTheme {
                MonthlyBarChart(summaries = listOf(summary))
            }
        }

        composeTestRule.onNodeWithText("Comparecimento por mês").assertIsDisplayed()
        
        // O formato do mês abreviado em pt-BR geralmente é "jan." (com ponto)
        // O texto completo deve ser "jan.: 50% (5/10)"
        composeTestRule.onNodeWithText("jan.: 50% (5/10)").assertIsDisplayed()
    }

    @Test
    fun statSummaryRow_displaysLabelAndValue() {
        val label = "Total de Dias"
        val value = "15"

        composeTestRule.setContent {
            PresencialTheme {
                StatSummaryRow(label = label, value = value)
            }
        }

        composeTestRule.onNodeWithText(label).assertIsDisplayed()
        composeTestRule.onNodeWithText(value).assertIsDisplayed()
    }
}
