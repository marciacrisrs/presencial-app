package com.presencial.app.presentation.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.CloudSyncState
import com.presencial.app.domain.model.GeofenceSyncStatus
import com.presencial.app.domain.model.PolicyValidationResult
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.model.WorkAddress
import com.presencial.app.ui.theme.PresencialTheme
import io.mockk.every
import io.mockk.mockk
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
    private val workAddressesFlow = MutableStateFlow<List<WorkAddress>>(emptyList())
    private val geofenceSyncStatusFlow = MutableStateFlow<GeofenceSyncStatus>(GeofenceSyncStatus.Unknown)
    private val policyValidationFlow = MutableStateFlow(PolicyValidationResult(isValid = true))
    private val cloudSyncStateFlow = MutableStateFlow(CloudSyncState())

    @Before
    fun setup() {
        every { viewModel.settings } returns settingsFlow
        every { viewModel.message } returns messageFlow
        every { viewModel.workAddresses } returns workAddressesFlow
        every { viewModel.geofenceSyncStatus } returns geofenceSyncStatusFlow
        every { viewModel.policyValidation } returns policyValidationFlow
        every { viewModel.cloudSyncState } returns cloudSyncStateFlow
        every { viewModel.pendingRestore } returns MutableStateFlow(null)
    }

    @Test
    fun settingsScreen_displaysInitialContent() {
        startSettingsScreen()

        composeTestRule.onNodeWithText("Configurações").assertIsDisplayed()
    }

    @Test
    fun changingPolicyPercentage_triggersViewModelUpdate() {
        startSettingsScreen()

        composeTestRule.onNodeWithText("60%").performScrollTo().performClick()
        verify { viewModel.updatePresencePolicy(match { it.freePercentage == 60 }) }
    }

    @Test
    fun togglingSaturdays_triggersViewModelUpdate() {
        startSettingsScreen()

        composeTestRule.onNode(
            isToggleable() and hasParent(hasAnyDescendant(hasText("Sábados como dias úteis")))
        ).performScrollTo().performClick()
        verify { viewModel.updateSaturdays(any()) }
    }

    @Test
    fun clickingOtherSettings_triggersNavigation() {
        var navigatedToAbout = false
        startSettingsScreen(onNavigateToAbout = { navigatedToAbout = true })

        composeTestRule.onNodeWithText("Sobre o Aplicativo").performScrollTo().performClick()

        org.junit.Assert.assertTrue(navigatedToAbout)
    }

    @Test
    fun restoreConfirmation_cancelDoesNotRestore() {
        val pendingRestoreFlow = MutableStateFlow<PendingRestore?>(PendingRestore.Folder)
        every { viewModel.pendingRestore } returns pendingRestoreFlow
        startSettingsScreen()

        composeTestRule.onNodeWithText("Restaurar backup?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").performClick()
        verify { viewModel.cancelRestore() }
        verify(exactly = 0) { viewModel.confirmRestore() }
        verify(exactly = 0) { viewModel.restoreCloudBackup() }
    }

    @Test
    fun restoreConfirmation_confirmCallsViewModel() {
        val pendingRestoreFlow = MutableStateFlow<PendingRestore?>(PendingRestore.Folder)
        every { viewModel.pendingRestore } returns pendingRestoreFlow
        startSettingsScreen()

        composeTestRule.onNodeWithText("Restaurar").performClick()
        verify { viewModel.confirmRestore() }
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
