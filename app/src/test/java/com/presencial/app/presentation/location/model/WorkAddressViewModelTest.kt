package com.presencial.app.presentation.location.model

import app.cash.turbine.test
import com.google.android.gms.location.FusedLocationProviderClient
import com.presencial.app.domain.location.GeocodingHelper
import com.presencial.app.domain.location.GeoCoordinates
import com.presencial.app.domain.repository.WorkAddressRepository
import com.presencial.app.domain.usecase.ResolveWorkAddressLocationUseCase
import com.presencial.app.domain.usecase.SyncGeofencesUseCase
import com.presencial.app.util.MainDispatcherExtension
import com.presencial.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class WorkAddressViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val repository = mockk<WorkAddressRepository>()
    private val syncGeofencesUseCase = mockk<SyncGeofencesUseCase>()
    private val resolveWorkAddressLocationUseCase = mockk<ResolveWorkAddressLocationUseCase>()
    private val geocodingHelper = mockk<GeocodingHelper>()
    private val fusedLocationProviderClient = mockk<FusedLocationProviderClient>()
    private lateinit var viewModel: WorkAddressViewModel

    @BeforeEach
    fun setup() {
        every { repository.getAllAddresses() } returns flowOf(emptyList())
        coEvery { syncGeofencesUseCase() } returns Unit
        coEvery { resolveWorkAddressLocationUseCase.resolve(any(), any()) } returns ("SP" to "São Paulo")
        viewModel = WorkAddressViewModel(
            repository,
            syncGeofencesUseCase,
            resolveWorkAddressLocationUseCase,
            geocodingHelper,
            fusedLocationProviderClient
        )
    }

    @Test
    fun `addresses should reflect repository flow`() = runTest {
        val addressList = listOf(TestDataFactory.createWorkAddress())
        every { repository.getAllAddresses() } returns flowOf(addressList)

        viewModel = WorkAddressViewModel(
            repository,
            syncGeofencesUseCase,
            resolveWorkAddressLocationUseCase,
            geocodingHelper,
            fusedLocationProviderClient
        )

        viewModel.addresses.test {
            assertEquals(addressList, awaitItem())
        }
    }

    @Test
    fun `saveWorkAddress should insert and sync geofences`() = runTest {
        coEvery { repository.insertAddress(any()) } returns Unit

        viewModel.saveWorkAddress(
            id = 0L,
            name = "Office",
            addressText = "Rua A",
            latitude = -23.0,
            longitude = -46.0,
            radius = 50f,
            isActive = true
        )

        coVerify {
            repository.insertAddress(
                match {
                    it.name == "Office" &&
                        it.latitude == -23.0 &&
                        it.longitude == -46.0 &&
                        it.stateCode == "SP" &&
                        it.cityName == "São Paulo"
                }
            )
        }
        coVerify { syncGeofencesUseCase() }
        assertEquals("Local salvo com sucesso", viewModel.message.value)
    }

    @Test
    fun `syncGeofences should not crash when registration fails`() = runTest {
        coEvery { syncGeofencesUseCase() } throws IllegalStateException("permission denied")

        viewModel.syncGeofences()

        coVerify(exactly = 1) { syncGeofencesUseCase() }
        assertEquals(
            "permission denied",
            viewModel.message.value
        )
    }

    @Test
    fun `geocodeAddress should update geocoded location on success`() = runTest {
        coEvery { geocodingHelper.geocodeAddress("Rua A") } returns Result.success(
            GeoCoordinates(-23.1, -46.1, "SP", "São Paulo")
        )

        viewModel.geocodeAddress("Rua A")

        assertEquals(-23.1 to -46.1, viewModel.geocodedLocation.value)
    }

    @Test
    fun `deleteAddress should call repository and sync geofences`() = runTest {
        val address = TestDataFactory.createWorkAddress()
        coEvery { repository.deleteAddress(address) } returns Unit

        viewModel.deleteAddress(address)

        coVerify { repository.deleteAddress(address) }
        coVerify { syncGeofencesUseCase() }
    }

    @Test
    fun `toggleActive should update address and sync geofences`() = runTest {
        val address = TestDataFactory.createWorkAddress(isActive = true)
        val updatedAddress = address.copy(isActive = false)
        coEvery { repository.updateAddress(updatedAddress) } returns Unit

        viewModel.toggleActive(address)

        coVerify { repository.updateAddress(updatedAddress) }
        coVerify { syncGeofencesUseCase() }
    }

    @Test
    fun `stopEditing should clear editingAddress`() {
        viewModel.startEditing(TestDataFactory.createWorkAddress())
        viewModel.stopEditing()
        assertNull(viewModel.editingAddress.value)
    }
}
