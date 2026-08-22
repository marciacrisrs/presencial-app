package com.presencial.app.domain.usecase

import com.presencial.app.domain.location.GeofenceRegistrar
import com.presencial.app.domain.location.GeofenceRegistrationException
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

        coVerify(exactly = 1) { geofenceRegistrar.registerGeofences(addresses) }
        coVerify { syncStatusRepository.markSuccess() }
    }

    @Test
    fun `when no active addresses, then remove geofences and mark success`() = runTest {
        coEvery { workAddressRepository.getActiveAddresses() } returns emptyList()

        useCase()

        coVerify(exactly = 1) { geofenceRegistrar.removeGeofences() }
        coVerify { syncStatusRepository.markSuccess() }
    }

    @Test
    fun `when transient removal fails, then retry and mark success after recovery`() = runTest {
        coEvery { workAddressRepository.getActiveAddresses() } returns emptyList()
        var attempts = 0
        coEvery { geofenceRegistrar.removeGeofences() } coAnswers {
            attempts++
            if (attempts < 3) {
                throw GeofenceRegistrationException("network unavailable", retryable = true)
            }
        }

        useCase()

        assert(attempts == 3)
        coVerify(exactly = 3) { geofenceRegistrar.removeGeofences() }
        coVerify { syncStatusRepository.markSuccess() }
        coVerify(exactly = 0) { syncStatusRepository.markFailure(any()) }
    }

    @Test
    fun `when transient removal never recovers, then persist final failure`() = runTest {
        coEvery { workAddressRepository.getActiveAddresses() } returns emptyList()
        coEvery { geofenceRegistrar.removeGeofences() } throws
            GeofenceRegistrationException("network unavailable", retryable = true)

        assertThrows<GeofenceRegistrationException> { useCase() }

        coVerify(exactly = 3) { geofenceRegistrar.removeGeofences() }
        coVerify { syncStatusRepository.markFailure("network unavailable") }
        coVerify(exactly = 0) { syncStatusRepository.markSuccess() }
    }

    @Test
    fun `when transient registration fails, then retry and mark success after recovery`() = runTest {
        val addresses = listOf(TestDataFactory.createWorkAddress(isActive = true))
        coEvery { workAddressRepository.getActiveAddresses() } returns addresses
        var attempts = 0
        coEvery { geofenceRegistrar.registerGeofences(addresses) } coAnswers {
            attempts++
            if (attempts < 3) {
                throw GeofenceRegistrationException("network unavailable", retryable = true)
            }
        }

        useCase()

        assert(attempts == 3)
        coVerify(exactly = 3) { geofenceRegistrar.registerGeofences(addresses) }
        coVerify { syncStatusRepository.markSuccess() }
        coVerify(exactly = 0) { syncStatusRepository.markFailure(any()) }
    }

    @Test
    fun `when transient registration never recovers, then persist final failure`() = runTest {
        val addresses = listOf(TestDataFactory.createWorkAddress(isActive = true))
        coEvery { workAddressRepository.getActiveAddresses() } returns addresses
        coEvery { geofenceRegistrar.registerGeofences(addresses) } throws
            GeofenceRegistrationException("network unavailable", retryable = true)

        assertThrows<GeofenceRegistrationException> { useCase() }

        coVerify(exactly = 3) { geofenceRegistrar.registerGeofences(addresses) }
        coVerify { syncStatusRepository.markFailure("network unavailable") }
        coVerify(exactly = 0) { syncStatusRepository.markSuccess() }
    }

    @Test
    fun `when registration failure is not retryable, then fail immediately`() = runTest {
        val addresses = listOf(TestDataFactory.createWorkAddress(isActive = true))
        coEvery { workAddressRepository.getActiveAddresses() } returns addresses
        coEvery { geofenceRegistrar.registerGeofences(addresses) } throws
            GeofenceRegistrationException("permission denied", retryable = false)

        assertThrows<GeofenceRegistrationException> { useCase() }

        coVerify(exactly = 1) { geofenceRegistrar.registerGeofences(addresses) }
        coVerify { syncStatusRepository.markFailure("permission denied") }
        coVerify(exactly = 0) { syncStatusRepository.markSuccess() }
    }

    @Test
    fun `when geofence registration fails with legacy illegal state exception, then persist failure and rethrow`() =
        runTest {
            coEvery { workAddressRepository.getActiveAddresses() } returns listOf(
                TestDataFactory.createWorkAddress(isActive = true)
            )
            coEvery { geofenceRegistrar.registerGeofences(any()) } throws IllegalStateException("permission denied")

            assertThrows<IllegalStateException> { useCase() }

            coVerify { syncStatusRepository.markFailure("permission denied") }
            coVerify(exactly = 0) { syncStatusRepository.markSuccess() }
        }
}
