package com.presencial.app.domain.location

object GeofencePermissionSync {
    fun shouldSync(foregroundGranted: Boolean, backgroundGranted: Boolean): Boolean =
        foregroundGranted && backgroundGranted
}
