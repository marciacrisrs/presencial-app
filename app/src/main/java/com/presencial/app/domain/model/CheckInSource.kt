package com.presencial.app.domain.model

object CheckInSource {
    const val MANUAL = "MANUAL"
    const val AUTO_GEOFENCE = "auto_geofence"

    /** Valor legado — mantido para exibição de registros antigos. */
    private const val LEGACY_AUTOMATICO = "AUTOMATICO"

    const val AUTO_GEOFENCE_LABEL = "Check-in automático"

    fun isAutoGeofence(source: String): Boolean =
        source == AUTO_GEOFENCE || source == LEGACY_AUTOMATICO
}
