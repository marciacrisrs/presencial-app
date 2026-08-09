package com.presencial.app.presentation.location

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.presencial.app.domain.model.WorkAddress
import com.presencial.app.presentation.location.model.WorkAddressViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WorkAddressFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel: WorkAddressViewModel = mockk(relaxed = true)
    
    private val addressesFlow = MutableStateFlow<List<WorkAddress>>(emptyList())
    private val editingAddressFlow = MutableStateFlow<WorkAddress?>(null)
    private val messageFlow = MutableStateFlow<String?>(null)

    @Before
    fun setup() {
        every { viewModel.addresses } returns addressesFlow
        every { viewModel.editingAddress } returns editingAddressFlow
        every { viewModel.message } returns messageFlow
    }

    @Test
    fun workAddressScreen_initialState_showsEmptyMessage() {
        addressesFlow.value = emptyList()

        composeTestRule.setContent {
            WorkAddressScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithText("Nenhum local cadastrado").assertExists()
    }

    @Test
    fun workAddressScreen_withAddresses_showsList() {
        val address = WorkAddress(id = 1, name = "Escritório", addressText = "Rua A, 123", latitude = 0.0, longitude = 0.0)
        addressesFlow.value = listOf(address)

        composeTestRule.setContent {
            WorkAddressScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithText("Escritório").assertExists()
        composeTestRule.onNodeWithText("Rua A, 123").assertExists()
    }

    @Test
    fun clickingFAB_opensNewLocationDialog() {
        composeTestRule.setContent {
            WorkAddressScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithContentDescription("Adicionar").performClick()

        verify { viewModel.startEditing(null) }
        
        // Simular que o ViewModel atualizou o editingAddress
        editingAddressFlow.value = WorkAddress(name = "", addressText = "", latitude = 0.0, longitude = 0.0)
        
        composeTestRule.onNodeWithText("Novo Local").assertExists()
    }

    @Test
    fun dialogValidation_saveButtonEnabledOnlyWithName() {
        // Mock permission state - assume granted for simplicity in this flow test
        // The dialog check: name.isNotBlank() && (permissionsGranted || !isNewAddress)
        // Since we are mocking the ViewModel, we need to ensure editingAddress is set to show the dialog
        editingAddressFlow.value = WorkAddress(name = "", addressText = "", latitude = 0.0, longitude = 0.0)

        composeTestRule.setContent {
            WorkAddressScreen(viewModel = viewModel, onBack = {})
        }

        // Initially disabled if name is empty
        composeTestRule.onNodeWithText("Salvar Local Atual").assertIsNotEnabled()

        // Type name
        composeTestRule.onNodeWithText("Nome do Local").performTextInput("Meu Trabalho")

        // Should be enabled
        composeTestRule.onNodeWithText("Salvar Local Atual").assertIsEnabled()
    }

    @Test
    fun saveAddress_triggersViewModelAction() {
        editingAddressFlow.value = WorkAddress(name = "", addressText = "", latitude = 0.0, longitude = 0.0)

        composeTestRule.setContent {
            WorkAddressScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithText("Nome do Local").performTextInput("Escritório Central")
        composeTestRule.onNodeWithText("Salvar Local Atual").performClick()

        verify { 
            viewModel.saveCurrentLocationAsWorkAddress(
                name = "Escritório Central",
                addressText = "",
                radius = 50f
            ) 
        }
    }
}
