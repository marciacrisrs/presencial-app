package com.presencial.app.data.sync

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudSyncPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val lastSyncEpochMillis = dataStore.data.map { prefs ->
        prefs[Keys.LAST_SYNC_EPOCH]?.takeIf { it > 0L }
    }

    suspend fun getLastSyncEpochMillis(): Long? = lastSyncEpochMillis.first()

    suspend fun setLastSyncEpochMillis(epochMillis: Long) {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_SYNC_EPOCH] = epochMillis
        }
    }

    suspend fun clearLastSyncEpochMillis() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.LAST_SYNC_EPOCH)
        }
    }

    private object Keys {
        val LAST_SYNC_EPOCH = longPreferencesKey("cloud_sync_last_epoch")
    }
}
