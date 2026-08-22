package com.presencial.app.domain.usecase

import com.presencial.app.domain.location.GeofenceRegistrar
import com.presencial.app.domain.location.GeofenceRegistrationException
import com.presencial.app.domain.repository.GeofenceSyncStatusRepository
import com.presencial.app.domain.repository.WorkAddressRepository
import java.util.concurrent.CancellationException
import kotlinx.coroutines.delay
import javax.inject.Inject

class SyncGeofencesUseCase @Inject constructor(
    private val workAddressRepository: WorkAddressRepository,
    private val geofenceRegistrar: GeofenceRegistrar,
    private val syncStatusRepository: GeofenceSyncStatusRepository
) {
    suspend operator fun invoke() {
        try {
            val activeAddresses = workAddressRepository.getActiveAddresses()
            syncWithRetry(activeAddresses)
            syncStatusRepository.markSuccess()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: IllegalStateException) {
            syncStatusRepository.markFailure(
                exception.message ?: "Não foi possível atualizar o monitoramento de localização."
            )
            throw exception
        }
    }

    private suspend fun syncWithRetry(activeAddresses: List<com.presencial.app.domain.model.WorkAddress>) {
        var attempt = 1

        while (true) {
            try {
                if (activeAddresses.isEmpty()) {
                    geofenceRegistrar.removeGeofences()
                } else {
                    geofenceRegistrar.registerGeofences(activeAddresses)
                }
                return
            } catch (exception: GeofenceRegistrationException) {
                if (!exception.retryable || attempt >= MAX_ATTEMPTS) {
                    throw exception
                }
                delay(RETRY_DELAYS_MS[attempt - 1])
                attempt++
            }
        }
    }

    private companion object {
        const val MAX_ATTEMPTS = 3
        val RETRY_DELAYS_MS = longArrayOf(1_000L, 2_000L)
    }
}
