package com.presencial.app.domain.usecase

import com.presencial.app.domain.location.GeofenceRegistrar
import com.presencial.app.domain.repository.GeofenceSyncStatusRepository
import com.presencial.app.domain.repository.WorkAddressRepository
import javax.inject.Inject

class SyncGeofencesUseCase @Inject constructor(
    private val workAddressRepository: WorkAddressRepository,
    private val geofenceRegistrar: GeofenceRegistrar,
    private val syncStatusRepository: GeofenceSyncStatusRepository
) {
    suspend operator fun invoke() {
        try {
            val activeAddresses = workAddressRepository.getActiveAddresses()
            if (activeAddresses.isEmpty()) {
                geofenceRegistrar.removeGeofences()
            } else {
                geofenceRegistrar.registerGeofences(activeAddresses)
            }
            syncStatusRepository.markSuccess()
        } catch (exception: RuntimeException) {
            syncStatusRepository.markFailure(
                exception.message ?: "Não foi possível atualizar o monitoramento de localização."
            )
            throw exception
        }
    }
}
