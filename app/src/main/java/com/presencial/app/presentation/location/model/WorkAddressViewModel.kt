package com.presencial.app.presentation.location.model

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.presencial.app.domain.location.GeofenceManager
import com.presencial.app.domain.model.WorkAddress
import com.presencial.app.domain.repository.WorkAddressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class WorkAddressViewModel @Inject constructor(
    private val repository: WorkAddressRepository,
    private val geofenceManager: GeofenceManager,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : ViewModel() {

    val addresses: StateFlow<List<WorkAddress>> = repository.getAllAddresses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val _editingAddress = MutableStateFlow<WorkAddress?>(null)
    val editingAddress: StateFlow<WorkAddress?> = _editingAddress

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    @SuppressLint("MissingPermission")
    fun saveCurrentLocationAsWorkAddress(name: String, addressText: String, radius: Float) {
        viewModelScope.launch {
            val result = runCatching {
                fusedLocationProviderClient.lastLocation.await()
            }
            
            result.onSuccess { location ->
                if (location != null) {
                    val addressToSave = _editingAddress.value?.copy(
                        name = name,
                        addressText = addressText,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        radius = radius
                    ) ?: WorkAddress(
                        name = name,
                        addressText = addressText,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        radius = radius
                    )
                    saveAddress(addressToSave)
                    _editingAddress.value = null
                } else {
                    _message.value = "Não foi possível obter a localização atual"
                }
            }.onFailure { e ->
                _message.value = "Erro ao obter localização: ${e.message}"
            }
        }
    }

    fun saveAddress(address: WorkAddress) {
        viewModelScope.launch {
            if (address.id == 0L) {
                repository.insertAddress(address)
            } else {
                repository.updateAddress(address)
            }
            updateGeofences()
            _message.value = "Local salvo com sucesso"
            _editingAddress.value = null
        }
    }

    fun startEditing(address: WorkAddress?) {
        _editingAddress.value = address ?: WorkAddress(
            name = "",
            addressText = "",
            latitude = 0.0,
            longitude = 0.0,
            radius = 50f
        )
    }

    fun stopEditing() {
        _editingAddress.value = null
    }

    fun deleteAddress(address: WorkAddress) {
        viewModelScope.launch {
            repository.deleteAddress(address)
            updateGeofences()
            _message.value = "Local removido"
        }
    }

    fun toggleActive(address: WorkAddress) {
        viewModelScope.launch {
            repository.updateAddress(address.copy(isActive = !address.isActive))
            updateGeofences()
        }
    }

    private suspend fun updateGeofences() {
        val activeAddresses = repository.getActiveAddresses()
        if (activeAddresses.isEmpty()) {
            geofenceManager.removeGeofences()
        } else {
            geofenceManager.registerGeofences(activeAddresses)
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

private const val STOP_TIMEOUT_MS = 5000L
