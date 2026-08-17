package com.presencial.app.domain.usecase

import com.presencial.app.domain.location.GeofenceRegistrar
import com.presencial.app.domain.repository.GeofenceSyncStatusRepository
import com.presencial.app.domain.repository.WorkAddressRepository
import com.presencial.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SyncGeofencesUseCaseTest {

    private val workAddressRepository = mockk<WorkAddressRepository>()
    private val geofenceRegistrar = mockk<GeofenceRegistrar>(relaxed = true)
    private val syncStatusRepository = mockk<GeofenceSyncStatusRepository>(relaxed = true)
    private lateinit var useCase: SyncGeofencesUseCase

    @BeforeEach
    fun setup() {
        useCase = SyncGeofencesUseCase(
            workAddressRepository,
            geofenceRegistrar,
            syncStatusRepository
        )
    }

    @Test
    fun `when active addresses exist, then register geofences and mark success`() = runTest {
        val addresses = listOf(TestDataFactory.createWorkAddress(isActive = true))
        coEvery { workAddressRepository.getActiveAddresses() } returns addresses

        useCase()

        coVerify { geofenceRegistrar.registerGeofences(addresses) }
        coVerify { syncStatusRepository.markSuccess() }
    }

    @Test
    fun `when no active addresses, then remove geofences and mark success`() = runTest {
        coEvery { workAddressRepository.getActiveAddresses() } returns emptyList()

        useCase()

        coVerify { geofenceRegistrar.removeGeofences() }
        coVerify { syncStatusRepository.markSuccess() }
    }

    @Test
    fun `when geofence registration fails, then persist failure and rethrow`() = runTest {
        coEvery { workAddressRepository.getActiveAddresses() } returns listOf(
            TestDataFactory.createWorkAddress(isActive = true)
        )
        coEvery { geofenceRegistrar.registerGeofences(any()) } throws IllegalStateException("permission denied")

        assertThrows<IllegalStateException> { useCase() }

        coVerify { syncStatusRepository.markFailure("permission denied") }
        coVerify(exactly = 0) { syncStatusRepository.markSuccess() }
    }
}
