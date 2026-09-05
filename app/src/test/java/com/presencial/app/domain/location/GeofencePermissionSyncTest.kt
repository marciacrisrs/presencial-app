package com.presencial.app.domain.location

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GeofencePermissionSyncTest {

    @Test
    fun `syncs only when foreground and background location are granted`() {
        assertTrue(GeofencePermissionSync.shouldSync(foregroundGranted = true, backgroundGranted = true))
        assertFalse(GeofencePermissionSync.shouldSync(foregroundGranted = true, backgroundGranted = false))
        assertFalse(GeofencePermissionSync.shouldSync(foregroundGranted = false, backgroundGranted = true))
        assertFalse(GeofencePermissionSync.shouldSync(foregroundGranted = false, backgroundGranted = false))
    }
}
