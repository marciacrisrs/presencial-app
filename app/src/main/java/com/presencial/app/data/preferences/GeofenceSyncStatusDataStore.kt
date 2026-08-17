package com.presencial.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.presencial.app.domain.model.GeofenceSyncStatus
import com.presencial.app.domain.repository.GeofenceSyncStatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceSyncStatusDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : GeofenceSyncStatusRepository {

    override val status: Flow<GeofenceSyncStatus> = dataStore.data.map { prefs ->
        when (prefs[STATUS]) {
            SUCCESS -> GeofenceSyncStatus.Success
            FAILURE -> GeofenceSyncStatus.Failure(prefs[MESSAGE].orEmpty())
            else -> GeofenceSyncStatus.Unknown
        }
    }

    override suspend fun markSuccess() {
        dataStore.edit { prefs ->
            prefs[STATUS] = SUCCESS
            prefs.remove(MESSAGE)
        }
    }

    override suspend fun markFailure(message: String) {
        dataStore.edit { prefs ->
            prefs[STATUS] = FAILURE
            prefs[MESSAGE] = message.take(MAX_MESSAGE_LENGTH)
        }
    }

    private companion object {
        val STATUS = stringPreferencesKey("geofence_sync_status")
        val MESSAGE = stringPreferencesKey("geofence_sync_error_message")
        const val SUCCESS = "success"
        const val FAILURE = "failure"
        const val MAX_MESSAGE_LENGTH = 300
    }
}
