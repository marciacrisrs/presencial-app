package com.presencial.app.domain.repository

import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.PresencePolicy
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun updateRequiredPercentage(percentage: Int)
    suspend fun updateCountSaturdaysAsWorkdays(count: Boolean)
    suspend fun updatePresencePolicy(policy: PresencePolicy)
    suspend fun updateOnboardingStep(step: Int)
    suspend fun completeOnboarding()
}
