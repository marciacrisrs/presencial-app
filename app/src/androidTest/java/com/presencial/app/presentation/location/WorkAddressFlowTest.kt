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
    private val isGeocodingFlow = MutableStateFlow(false)
    private val geocodedLocationFlow = MutableStateFlow<Pair<Double, Double>?>(null)
    private val currentGpsLocationFlow = MutableStateFlow<Pair<Double, Double>?>(null)

    @Before
    fun setup() {
        every { viewModel.addresses } returns addressesFlow
        every { viewModel.editingAddress } returns editingAddressFlow
        every { viewModel.message } returns messageFlow
        every { viewModel.isGeocoding } returns isGeocodingFlow
        every { viewModel.geocodedLocation } returns geocodedLocationFlow
        every { viewModel.currentGpsLocation } returns currentGpsLocationFlow
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
        val address = WorkAddress(
            id = 1,
            name = "Escritório",
            addressText = "Rua A, 123",
            latitude = -23.0,
            longitude = -46.0
        )
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

        editingAddressFlow.value = WorkAddress(name = "", addressText = "", latitude = 0.0, longitude = 0.0)

        composeTestRule.onNodeWithText("Novo Local").assertExists()
    }

    @Test
    fun dialogValidation_saveButtonDisabledWithoutCoordinates() {
        editingAddressFlow.value = WorkAddress(name = "", addressText = "", latitude = 0.0, longitude = 0.0)

        composeTestRule.setContent {
            WorkAddressScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithText("Salvar Local").assertIsNotEnabled()

        composeTestRule.onNodeWithText("Nome do Local").performTextInput("Meu Trabalho")

        composeTestRule.onNodeWithText("Salvar Local").assertIsNotEnabled()
    }

    @Test
    fun dialogValidation_saveButtonEnabledWithNameAndCoordinates() {
        editingAddressFlow.value = WorkAddress(
            name = "",
            addressText = "",
            latitude = -23.0,
            longitude = -46.0
        )
        geocodedLocationFlow.value = -23.0 to -46.0

        composeTestRule.setContent {
            WorkAddressScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithText("Nome do Local").performTextInput("Meu Trabalho")

        composeTestRule.onNodeWithText("Salvar Local").assertIsEnabled()
    }

    @Test
    fun saveAddress_triggersViewModelAction() {
        editingAddressFlow.value = WorkAddress(
            name = "",
            addressText = "",
            latitude = -23.0,
            longitude = -46.0
        )
        geocodedLocationFlow.value = -23.0 to -46.0

        composeTestRule.setContent {
            WorkAddressScreen(viewModel = viewModel, onBack = {})
        }

        composeTestRule.onNodeWithText("Nome do Local").performTextInput("Escritório Central")
        composeTestRule.onNodeWithText("Salvar Local").performClick()

        verify {
            viewModel.saveWorkAddress(
                id = 0L,
                name = "Escritório Central",
                addressText = "",
                latitude = -23.0,
                longitude = -46.0,
                radius = 50f,
                isActive = true
            )
        }
    }
}
