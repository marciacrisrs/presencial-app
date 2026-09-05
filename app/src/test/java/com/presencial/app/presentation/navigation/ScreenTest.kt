package com.presencial.app.presentation.navigation

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScreenTest {

    @Test
    fun `check-in notification waits until the nav graph is ready`() {
        assertFalse(
            Screen.shouldOpenHomeFromCheckInNotification(
                openCheckIn = true,
                currentRoute = null,
                currentPage = 0
            )
        )
    }

    @Test
    fun `check-in notification navigates when the user is on another tab`() {
        assertTrue(
            Screen.shouldOpenHomeFromCheckInNotification(
                openCheckIn = true,
                currentRoute = Screen.mainRoute(2),
                currentPage = 2
            )
        )
    }

    @Test
    fun `check-in notification stays put when already on home`() {
        assertFalse(
            Screen.shouldOpenHomeFromCheckInNotification(
                openCheckIn = true,
                currentRoute = Screen.mainRoute(0),
                currentPage = 0
            )
        )
    }

    @Test
    fun `check-in notification is ignored when the extra is not set`() {
        assertFalse(
            Screen.shouldOpenHomeFromCheckInNotification(
                openCheckIn = false,
                currentRoute = Screen.About.route,
                currentPage = 0
            )
        )
    }
}
