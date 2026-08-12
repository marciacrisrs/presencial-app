package com.presencial.app.domain.model

/**
 * Configurações do aplicativo persistidas no DataStore.
 */
data class AppSettings(
    val requiredPercentage: Int = 40,
    val countSaturdaysAsWorkdays: Boolean = false,
    val presencePolicy: PresencePolicy = PresencePolicy.fromLegacyPercentage(40)
) {
    fun synced(): AppSettings {
        val effectivePercentage = presencePolicy.freePercentage
        return copy(
            requiredPercentage = effectivePercentage,
            presencePolicy = presencePolicy.copy(freePercentage = effectivePercentage)
        )
    }
}
