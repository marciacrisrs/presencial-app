package com.presencial.app.domain.usecase

import com.presencial.app.domain.location.GeofenceRegistrar
import com.presencial.app.domain.repository.WorkAddressRepository
import com.presencial.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SyncGeofencesUseCaseTest {

    private val workAddressRepository = mockk<WorkAddressRepository>()
    private val geofenceRegistrar = mockk<GeofenceRegistrar>(relaxed = true)
    private lateinit var useCase: SyncGeofencesUseCase

    @BeforeEach
    fun setup() {
        useCase = SyncGeofencesUseCase(workAddressRepository, geofenceRegistrar)
    }

    @Test
    fun `when active addresses exist, then register geofences`() = runTest {
        val addresses = listOf(TestDataFactory.createWorkAddress(isActive = true))
        coEvery { workAddressRepository.getActiveAddresses() } returns addresses

        useCase()

        coVerify { geofenceRegistrar.registerGeofences(addresses) }
    }

    @Test
    fun `when no active addresses, then remove geofences`() = runTest {
        coEvery { workAddressRepository.getActiveAddresses() } returns emptyList()

        useCase()

        coVerify { geofenceRegistrar.removeGeofences() }
    }
}
