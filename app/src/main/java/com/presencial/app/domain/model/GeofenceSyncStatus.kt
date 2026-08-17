package com.presencial.app.domain.model

sealed interface GeofenceSyncStatus {
    data object Unknown : GeofenceSyncStatus
    data object Success : GeofenceSyncStatus
    data class Failure(val message: String) : GeofenceSyncStatus
}
