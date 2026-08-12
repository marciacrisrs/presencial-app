package com.presencial.app.domain.usecase

import com.presencial.app.domain.location.GeofenceManager
import com.presencial.app.domain.repository.WorkAddressRepository
import javax.inject.Inject

class SyncGeofencesUseCase @Inject constructor(
    private val workAddressRepository: WorkAddressRepository,
    private val geofenceManager: GeofenceManager
) {
    suspend operator fun invoke() {
        val activeAddresses = workAddressRepository.getActiveAddresses()
        if (activeAddresses.isEmpty()) {
            geofenceManager.removeGeofences()
        } else {
            geofenceManager.registerGeofences(activeAddresses)
        }
    }
}
