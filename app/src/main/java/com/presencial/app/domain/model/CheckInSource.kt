package com.presencial.app.domain.model

object CheckInSource {
    const val MANUAL = "MANUAL"
    const val AUTO_GEOFENCE = "auto_geofence"

    /** Valor legado — mantido para exibição de registros antigos. */
    private const val LEGACY_AUTOMATICO = "AUTOMATICO"

    fun isAutoGeofence(source: String): Boolean =
        source == AUTO_GEOFENCE || source == LEGACY_AUTOMATICO

    fun autoGeofenceLabel(): String = "Check-in automático"
}
