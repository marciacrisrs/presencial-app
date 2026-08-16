package com.presencial.app.presentation.components

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import com.presencial.app.ui.theme.PresencialTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MonitoringStatusBannerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun banner_click_invokesNavigationCallback() {
        var clicked = false

        composeTestRule.setContent {
            PresencialTheme {
                MonitoringStatusBanner(
                    activeAddressCount = 1,
                    foregroundGranted = false,
                    backgroundGranted = false,
                    onClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNode(hasClickAction()).performClick()

        assertTrue(clicked)
    }
}
