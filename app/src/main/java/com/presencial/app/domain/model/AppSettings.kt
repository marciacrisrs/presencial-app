package com.presencial.app.domain.model

/**
 * Configurações do aplicativo persistidas no DataStore.
 */
data class AppSettings(
    val requiredPercentage: Int = 40,
    val countSaturdaysAsWorkdays: Boolean = false
)
