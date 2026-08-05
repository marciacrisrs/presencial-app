package com.presencial.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "presencial_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object Keys {
        val REQUIRED_PERCENTAGE = intPreferencesKey("required_percentage")
        val COUNT_SATURDAYS = booleanPreferencesKey("count_saturdays_as_workdays")
    }

    override val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            requiredPercentage = prefs[Keys.REQUIRED_PERCENTAGE] ?: 40,
            countSaturdaysAsWorkdays = prefs[Keys.COUNT_SATURDAYS] ?: false
        )
    }

    override suspend fun updateRequiredPercentage(percentage: Int) {
        val allowedValues = listOf(20, 40, 60)
        val value = if (percentage in allowedValues) percentage else 40
        context.dataStore.edit { it[Keys.REQUIRED_PERCENTAGE] = value }
        syncToSharedPreferences(value, null)
    }

    override suspend fun updateCountSaturdaysAsWorkdays(count: Boolean) {
        context.dataStore.edit { it[Keys.COUNT_SATURDAYS] = count }
        syncToSharedPreferences(null, count)
    }

    private fun syncToSharedPreferences(percentage: Int?, countSaturdays: Boolean?) {
        context.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE).edit().apply {
            percentage?.let { putInt("required_percentage", it) }
            countSaturdays?.let { putBoolean("count_saturdays_as_workdays", it) }
            apply()
        }
    }

    companion object {
        private const val WIDGET_PREFS = "presencial_settings"
    }
}
