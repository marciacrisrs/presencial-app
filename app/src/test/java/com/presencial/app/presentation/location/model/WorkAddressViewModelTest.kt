package com.presencial.app.presentation.location.model

import android.location.Location
import app.cash.turbine.test
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task
import com.presencial.app.domain.location.GeofenceManager
import com.presencial.app.domain.model.WorkAddress
import com.presencial.app.domain.repository.WorkAddressRepository
import com.presencial.app.util.MainDispatcherExtension
import com.presencial.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class WorkAddressViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val repository = mockk<WorkAddressRepository>()
    private val geofenceManager = mockk<GeofenceManager>()
    private val fusedLocationProviderClient = mockk<FusedLocationProviderClient>()
    private lateinit var viewModel: WorkAddressViewModel

    @BeforeEach
    fun setup() {
        every { repository.getAllAddresses() } returns flowOf(emptyList())
        viewModel = WorkAddressViewModel(repository, geofenceManager, fusedLocationProviderClient)
    }

    @Test
    fun `addresses should reflect repository flow`() = runTest {
        val addressList = listOf(TestDataFactory.createWorkAddress())
        every { repository.getAllAddresses() } returns flowOf(addressList)
        
        viewModel = WorkAddressViewModel(repository, geofenceManager, fusedLocationProviderClient)

        viewModel.addresses.test {
            assertEquals(addressList, awaitItem())
        }
    }

    @Test
    fun `startEditing should update editingAddress`() {
        val address = TestDataFactory.createWorkAddress()
        viewModel.startEditing(address)
        assertEquals(address, viewModel.editingAddress.value)
    }

    @Test
    fun `stopEditing should clear editingAddress`() {
        viewModel.startEditing(TestDataFactory.createWorkAddress())
        viewModel.stopEditing()
        assertNull(viewModel.editingAddress.value)
    }

    @Test
    fun `saveAddress should call repository and update geofences`() = runTest {
        val address = TestDataFactory.createWorkAddress(id = 0L)
        coEvery { repository.insertAddress(address) } returns Unit
        coEvery { repository.getActiveAddresses() } returns emptyList()
        coEvery { geofenceManager.removeGeofences() } returns Unit

        viewModel.saveAddress(address)

        coVerify { repository.insertAddress(address) }
        coVerify { geofenceManager.removeGeofences() }
        assertEquals("Local salvo com sucesso", viewModel.message.value)
        assertNull(viewModel.editingAddress.value)
    }

    @Test
    fun `deleteAddress should call repository and update geofences`() = runTest {
        val address = TestDataFactory.createWorkAddress()
        coEvery { repository.deleteAddress(address) } returns Unit
        coEvery { repository.getActiveAddresses() } returns emptyList()
        coEvery { geofenceManager.removeGeofences() } returns Unit

        viewModel.deleteAddress(address)

        coVerify { repository.deleteAddress(address) }
        coVerify { geofenceManager.removeGeofences() }
        assertEquals("Local removido", viewModel.message.value)
    }

    @Test
    fun `toggleActive should update address and geofences`() = runTest {
        val address = TestDataFactory.createWorkAddress(isActive = true)
        val updatedAddress = address.copy(isActive = false)
        coEvery { repository.updateAddress(updatedAddress) } returns Unit
        coEvery { repository.getActiveAddresses() } returns emptyList()
        coEvery { geofenceManager.removeGeofences() } returns Unit

        viewModel.toggleActive(address)

        coVerify { repository.updateAddress(updatedAddress) }
        coVerify { geofenceManager.removeGeofences() }
    }

    @Test
    fun `saveCurrentLocationAsWorkAddress should handle location success`() = runTest {
        val location = mockk<Location>()
        every { location.latitude } returns -23.0
        every { location.longitude } returns -46.0
        
        val task = mockk<Task<Location>>()
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.result } returns location
        every { task.exception } returns null
        // Note: Task.await() is an extension property/function. 
        // Mocking it might be tricky depending on how it's imported.
        // FusedLocationProviderClient.lastLocation returns Task<Location>
        every { fusedLocationProviderClient.lastLocation } returns task

        coEvery { repository.insertAddress(any()) } returns Unit
        coEvery { repository.getActiveAddresses() } returns emptyList()
        coEvery { geofenceManager.removeGeofences() } returns Unit

        viewModel.saveCurrentLocationAsWorkAddress("Home", "Street 1", 100f)

        coVerify { repository.insertAddress(match { 
            it.name == "Home" && it.latitude == -23.0 && it.longitude == -46.0 
        }) }
    }
}
