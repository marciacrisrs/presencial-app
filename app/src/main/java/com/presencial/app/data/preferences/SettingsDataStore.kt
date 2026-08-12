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
    private val dataStore: DataStore<Preferences>,
    private val openAiApiKeyStore: OpenAiApiKeyStore
) : SettingsRepository {

    init {
        runBlocking { migrateLegacyOpenAiKey() }
    }

    private object Keys {
        val REQUIRED_PERCENTAGE = intPreferencesKey("required_percentage")
        val COUNT_SATURDAYS = booleanPreferencesKey("count_saturdays_as_workdays")
        val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
        val PRESENCE_POLICY = stringPreferencesKey("presence_policy_json")
    }

    override val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        val percentage = prefs[Keys.REQUIRED_PERCENTAGE] ?: DEFAULT_PERCENTAGE
        val policy = PresencePolicyMapper.fromJson(prefs[Keys.PRESENCE_POLICY], percentage)
        AppSettings(
            requiredPercentage = percentage,
            countSaturdaysAsWorkdays = prefs[Keys.COUNT_SATURDAYS] ?: false,
            openAiApiKey = openAiApiKeyStore.read(),
            presencePolicy = policy
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

    override suspend fun updateOpenAiApiKey(apiKey: String) {
        openAiApiKeyStore.save(apiKey)
        dataStore.edit { it.remove(Keys.OPENAI_API_KEY) }
    }

    override suspend fun updatePresencePolicy(policy: PresencePolicy) {
        val normalized = policy.normalized()
        val percentage = if (normalized.freePercentageEnabled) {
            normalized.freePercentage
        } else {
            settings.first().requiredPercentage
        }
        persistPolicy(percentage, null, normalized)
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

    private suspend fun migrateLegacyOpenAiKey() {
        val prefs = dataStore.data.first()
        val legacyKey = prefs[Keys.OPENAI_API_KEY] ?: return
        if (legacyKey.isNotBlank() && openAiApiKeyStore.read().isBlank()) {
            openAiApiKeyStore.save(legacyKey)
        }
        dataStore.edit { it.remove(Keys.OPENAI_API_KEY) }
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
    }
}
