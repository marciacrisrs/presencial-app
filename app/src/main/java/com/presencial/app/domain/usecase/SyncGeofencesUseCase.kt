package com.presencial.app.domain.usecase

import com.presencial.app.domain.location.GeofenceRegistrar
import com.presencial.app.domain.location.GeofenceRegistrationException
import com.presencial.app.domain.repository.GeofenceSyncStatusRepository
import com.presencial.app.domain.repository.WorkAddressRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncGeofencesUseCase @Inject constructor(
    private val workAddressRepository: WorkAddressRepository,
    private val geofenceRegistrar: GeofenceRegistrar,
    private val syncStatusRepository: GeofenceSyncStatusRepository
) {
    private val mutex = Mutex()

    suspend operator fun invoke() {
        mutex.withLock {
            try {
                syncWithRetry()
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
    }

    private suspend fun syncWithRetry() {
        var attempt = 1

        while (true) {
            val activeAddresses = workAddressRepository.getActiveAddresses()
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
