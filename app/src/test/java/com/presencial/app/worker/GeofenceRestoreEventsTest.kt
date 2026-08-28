package com.presencial.app.worker

import android.content.Intent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeofenceRestoreEventsTest {

    @Test
    fun isSupported_acceptsBootAndSystemRestoreActions() {
        assertTrue(GeofenceRestoreEvents.isSupported(Intent.ACTION_BOOT_COMPLETED))
        assertTrue(GeofenceRestoreEvents.isSupported(Intent.ACTION_LOCKED_BOOT_COMPLETED))
        assertTrue(GeofenceRestoreEvents.isSupported(Intent.ACTION_MY_PACKAGE_REPLACED))
        assertTrue(GeofenceRestoreEvents.isSupported(GeofenceRestoreEvents.ACTION_QUICKBOOT_POWERON))
    }

    @Test
    fun isSupported_rejectsUnknownActions() {
        assertFalse(GeofenceRestoreEvents.isSupported(Intent.ACTION_VIEW))
        assertFalse(GeofenceRestoreEvents.isSupported(null))
    }

    @Test
    fun shouldRetryRestore_retriesUntilMaxAttempts() {
        assertTrue(GeofenceRestoreEvents.shouldRetryRestore(0))
        assertTrue(GeofenceRestoreEvents.shouldRetryRestore(3))
        assertFalse(GeofenceRestoreEvents.shouldRetryRestore(4))
        assertFalse(GeofenceRestoreEvents.shouldRetryRestore(5))
    }
}
