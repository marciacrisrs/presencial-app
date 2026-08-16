package com.presencial.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.OnboardingEligibility
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    init {
        runBlocking { cleanupLegacyPreferences() }
    }

    private object Keys {
        val REQUIRED_PERCENTAGE = intPreferencesKey("required_percentage")
        val COUNT_SATURDAYS = booleanPreferencesKey("count_saturdays_as_workdays")
        val PRESENCE_POLICY = stringPreferencesKey("presence_policy_json")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ONBOARDING_STEP = intPreferencesKey("onboarding_step")
    }

    override val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        val percentage = prefs[Keys.REQUIRED_PERCENTAGE] ?: DEFAULT_PERCENTAGE
        val policy = PresencePolicyMapper.fromJson(prefs[Keys.PRESENCE_POLICY], percentage)
        AppSettings(
            requiredPercentage = percentage,
            countSaturdaysAsWorkdays = prefs[Keys.COUNT_SATURDAYS] ?: false,
            presencePolicy = policy,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            onboardingStep = OnboardingEligibility.coerceStep(
                prefs[Keys.ONBOARDING_STEP] ?: OnboardingEligibility.STEP_GOAL
            )
        ).synced()
    }

    override suspend fun updateRequiredPercentage(percentage: Int) {
        val value = percentage.coerceIn(PresencePolicy.MIN_PERCENTAGE, PresencePolicy.MAX_PERCENTAGE)
        val current = settings.first()
        val updatedPolicy = current.presencePolicy.copy(
            freePercentageEnabled = true,
            freePercentage = value
        )
        persistPolicy(value, null, updatedPolicy)
    }

    override suspend fun updateCountSaturdaysAsWorkdays(count: Boolean) {
        dataStore.edit { it[Keys.COUNT_SATURDAYS] = count }
        syncToSharedPreferences(null, count, null)
    }

    override suspend fun updatePresencePolicy(policy: PresencePolicy) {
        val normalized = policy.normalized()
        persistPolicy(normalized.freePercentage, null, normalized)
    }

    override suspend fun restoreBackupSettings(
        requiredPercentage: Int,
        countSaturdaysAsWorkdays: Boolean,
        presencePolicy: PresencePolicy?
    ) {
        val percentage = requiredPercentage.coerceIn(PresencePolicy.MIN_PERCENTAGE, PresencePolicy.MAX_PERCENTAGE)
        val current = settings.first()
        val restoredPolicy = presencePolicy?.normalized()
            ?: current.presencePolicy.copy(
                freePercentageEnabled = true,
                freePercentage = percentage
            )
        dataStore.edit { prefs ->
            prefs[Keys.REQUIRED_PERCENTAGE] = percentage
            prefs[Keys.COUNT_SATURDAYS] = countSaturdaysAsWorkdays
            prefs[Keys.PRESENCE_POLICY] = PresencePolicyMapper.toJson(restoredPolicy)
        }
        syncToSharedPreferences(percentage, countSaturdaysAsWorkdays, restoredPolicy)
    }

    override suspend fun updateOnboardingStep(step: Int) {
        dataStore.edit {
            it[Keys.ONBOARDING_STEP] = OnboardingEligibility.coerceStep(step)
        }
    }

    override suspend fun completeOnboarding() {
        dataStore.edit {
            it[Keys.ONBOARDING_COMPLETED] = true
            it[Keys.ONBOARDING_STEP] = OnboardingEligibility.STEP_LOCATION
        }
    }

    private suspend fun persistPolicy(
        percentage: Int,
        countSaturdays: Boolean?,
        policy: PresencePolicy
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.REQUIRED_PERCENTAGE] = percentage
            prefs[Keys.PRESENCE_POLICY] = PresencePolicyMapper.toJson(policy)
            countSaturdays?.let { prefs[Keys.COUNT_SATURDAYS] = it }
        }
        syncToSharedPreferences(percentage, countSaturdays, policy)
    }

    private suspend fun cleanupLegacyPreferences() {
        dataStore.edit { it.remove(LEGACY_OPENAI_API_KEY) }
    }

    private fun syncToSharedPreferences(
        percentage: Int?,
        countSaturdays: Boolean?,
        policy: PresencePolicy?
    ) {
        context.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE).edit().apply {
            percentage?.let { putInt("required_percentage", it) }
            countSaturdays?.let { putBoolean("count_saturdays_as_workdays", it) }
            policy?.let { putString("presence_policy_json", PresencePolicyMapper.toJson(it)) }
            apply()
        }
    }

    companion object {
        private const val WIDGET_PREFS = "presencial_settings"
        private const val DEFAULT_PERCENTAGE = 40
        private val LEGACY_OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
    }
}
