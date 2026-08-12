package com.presencial.app.presentation.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.PolicyValidationResult
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.ui.theme.PresencialTheme
import io.mockk.any
import io.mockk.every
import io.mockk.mockk
import io.mockk.match
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<SettingsViewModel>(relaxed = true)
    private val settingsFlow = MutableStateFlow(AppSettings())
    private val messageFlow = MutableStateFlow<String?>(null)

    private val policyValidationFlow = MutableStateFlow(PolicyValidationResult(isValid = true))
    private val weeklySummariesFlow = MutableStateFlow(emptyList<com.presencial.app.domain.model.WeeklyPolicySummary>())

    @Before
    fun setup() {
        every { viewModel.settings } returns settingsFlow
        every { viewModel.message } returns messageFlow
        every { viewModel.policyValidation } returns policyValidationFlow
        every { viewModel.weeklySummaries } returns weeklySummariesFlow
    }

    @Test
    fun settingsScreen_displaysInitialContent() {
        startSettingsScreen()

        composeTestRule.onNodeWithText("Configurações").assertIsDisplayed()
    }

    @Test
    fun changingPolicyPercentage_triggersViewModelUpdate() {
        startSettingsScreen()

        composeTestRule.onNodeWithText("60%").performClick()
        verify { viewModel.updatePresencePolicy(match { it.freePercentage == 60 }) }
    }

    @Test
    fun togglingSaturdays_triggersViewModelUpdate() {
        startSettingsScreen()

        // O Switch pode ser encontrado pelo seu estado de toggle
        composeTestRule.onNode(isToggleable()).performClick()
        verify { viewModel.updateSaturdays(any()) }
    }

    @Test
    fun clickingOtherSettings_triggersNavigation() {
        var navigatedToAbout = false
        startSettingsScreen(onNavigateToAbout = { navigatedToAbout = true })

        // "Sobre o Aplicativo" está dentro de um Card de configurações extras
        composeTestRule.onNodeWithText("Sobre o Aplicativo").performClick()
        
        assert(navigatedToAbout)
    }

    private fun startSettingsScreen(
        onNavigateToAbout: () -> Unit = {},
        onNavigateToAbsences: () -> Unit = {},
        onNavigateToWorkAddresses: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            PresencialTheme {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToAbout = onNavigateToAbout,
                    onNavigateToAbsences = onNavigateToAbsences,
                    onNavigateToWorkAddresses = onNavigateToWorkAddresses
                )
            }
        }
    }
}
