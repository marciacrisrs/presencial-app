package com.presencial.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object Keys {
        val REQUIRED_PERCENTAGE = intPreferencesKey("required_percentage")
        val COUNT_SATURDAYS = booleanPreferencesKey("count_saturdays_as_workdays")
    }

    override val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            requiredPercentage = prefs[Keys.REQUIRED_PERCENTAGE] ?: DEFAULT_PERCENTAGE,
            countSaturdaysAsWorkdays = prefs[Keys.COUNT_SATURDAYS] ?: false
        )
    }

    override suspend fun updateRequiredPercentage(percentage: Int) {
        val allowedValues = listOf(PERCENT_20, PERCENT_40, PERCENT_60)
        val value = if (percentage in allowedValues) percentage else DEFAULT_PERCENTAGE
        dataStore.edit { it[Keys.REQUIRED_PERCENTAGE] = value }
        syncToSharedPreferences(value, null)
    }

    override suspend fun updateCountSaturdaysAsWorkdays(count: Boolean) {
        dataStore.edit { it[Keys.COUNT_SATURDAYS] = count }
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
        private const val DEFAULT_PERCENTAGE = 40
        private const val PERCENT_20 = 20
        private const val PERCENT_40 = 40
        private const val PERCENT_60 = 60
    }
}
