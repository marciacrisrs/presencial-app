package com.presencial.app.domain.usecase

import com.presencial.app.domain.location.GeofenceRegistrar
import com.presencial.app.domain.repository.WorkAddressRepository
import javax.inject.Inject

class SyncGeofencesUseCase @Inject constructor(
    private val workAddressRepository: WorkAddressRepository,
    private val geofenceRegistrar: GeofenceRegistrar
) {
    suspend operator fun invoke() {
        val activeAddresses = workAddressRepository.getActiveAddresses()
        if (activeAddresses.isEmpty()) {
            geofenceRegistrar.removeGeofences()
        } else {
            geofenceRegistrar.registerGeofences(activeAddresses)
        }
    }
}
