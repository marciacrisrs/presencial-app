package com.presencial.app.presentation.location.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.presencial.app.ui.theme.PresencialTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddressFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addressField_displaysInitialValue() {
        val initialValue = "Rua Teste, 123"

        composeTestRule.setContent {
            PresencialTheme {
                AddressField(
                    value = initialValue,
                    onValueChange = {},
                )
            }
        }

        composeTestRule.onNodeWithText(initialValue).assertIsDisplayed()
    }

    @Test
    fun addressField_displaysCorrectLabel() {
        composeTestRule.setContent {
            PresencialTheme {
                AddressField(
                    value = "",
                    onValueChange = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Endereço (opcional)").assertIsDisplayed()
    }

    @Test
    fun addressField_callsOnValueChangeWhenTextIsInput() {
        var updatedValue = ""
        val newValue = "Novo Endereço"

        composeTestRule.setContent {
            PresencialTheme {
                AddressField(
                    value = "",
                    onValueChange = { updatedValue = it },
                )
            }
        }

        composeTestRule.onNodeWithText("Endereço (opcional)").performTextInput(newValue)

        assert(updatedValue == newValue)
    }
}
