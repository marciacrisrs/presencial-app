package com.presencial.app.domain.repository

import com.presencial.app.domain.model.GeofenceSyncStatus
import kotlinx.coroutines.flow.Flow

interface GeofenceSyncStatusRepository {
    val status: Flow<GeofenceSyncStatus>

    suspend fun markSuccess()

    suspend fun markFailure(message: String)
}
