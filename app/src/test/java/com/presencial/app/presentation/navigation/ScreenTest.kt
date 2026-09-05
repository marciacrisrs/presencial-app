package com.presencial.app.presentation.navigation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScreenTest {

    @Test
    fun `bottom navigation has four primary destinations without statistics`() {
        assertEquals(
            listOf(Screen.Dashboard, Screen.Calendar, Screen.History, Screen.Settings),
            Screen.bottomNavItems
        )
        assertFalse(Screen.bottomNavItems.contains(Screen.Statistics))
        assertEquals("statistics", Screen.Statistics.route)
    }

    @Test
    fun `legacy settings tab index restores to the current settings pager slot`() {
        assertEquals(3, Screen.pagerIndexFromSavedTab(4))
    }

    @Test
    fun `current settings tab index stays on settings`() {
        assertEquals(3, Screen.pagerIndexFromSavedTab(3))
    }

    @Test
    fun `out of range tab indexes clamp to the last primary destination`() {
        assertEquals(3, Screen.pagerIndexFromSavedTab(8))
        assertEquals(0, Screen.pagerIndexFromSavedTab(-1))
    }

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

    @Test
    fun `statistics is a dedicated route and not a main tab destination`() {
        assertFalse(Screen.isMainDestination(Screen.Statistics.route))
        assertTrue(Screen.isMainDestination(Screen.mainRoute(2)))
    }
}
