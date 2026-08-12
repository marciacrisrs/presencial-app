package com.presencial.app.domain.location

import com.presencial.app.domain.usecase.SyncGeofencesUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BootCompletedHandler @Inject constructor(
    private val syncGeofencesUseCase: SyncGeofencesUseCase
) {
    suspend fun handleBootCompleted() {
        syncGeofencesUseCase()
    }
}
