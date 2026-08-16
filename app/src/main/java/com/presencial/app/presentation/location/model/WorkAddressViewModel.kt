package com.presencial.app.presentation.location.model

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.presencial.app.domain.location.GeocodingHelper
import com.presencial.app.domain.model.WorkAddress
import com.presencial.app.domain.repository.WorkAddressRepository
import com.presencial.app.domain.usecase.ResolveWorkAddressLocationUseCase
import com.presencial.app.domain.usecase.SyncGeofencesUseCase
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
    private val syncGeofencesUseCase: SyncGeofencesUseCase,
    private val resolveWorkAddressLocationUseCase: ResolveWorkAddressLocationUseCase,
    private val geocodingHelper: GeocodingHelper,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : ViewModel() {

    val addresses: StateFlow<List<WorkAddress>> = repository.getAllAddresses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val _editingAddress = MutableStateFlow<WorkAddress?>(null)
    val editingAddress: StateFlow<WorkAddress?> = _editingAddress

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _isGeocoding = MutableStateFlow(false)
    val isGeocoding: StateFlow<Boolean> = _isGeocoding

    private val _geocodedLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val geocodedLocation: StateFlow<Pair<Double, Double>?> = _geocodedLocation

    private val _currentGpsLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val currentGpsLocation: StateFlow<Pair<Double, Double>?> = _currentGpsLocation

    fun saveWorkAddress(
        id: Long,
        name: String,
        addressText: String,
        latitude: Double,
        longitude: Double,
        radius: Float,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            val (stateCode, cityName) = resolveWorkAddressLocationUseCase.resolve(latitude, longitude)
            val address = WorkAddress(
                id = id,
                name = name,
                addressText = addressText,
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                isActive = isActive,
                stateCode = stateCode,
                cityName = cityName
            )
            if (id == 0L) {
                repository.insertAddress(address)
            } else {
                repository.updateAddress(address)
            }
            syncGeofences()
            _message.value = "Local salvo com sucesso"
            clearDialogState()
        }
    }

    fun syncGeofences() {
        viewModelScope.launch {
            syncGeofencesUseCase()
        }
    }

    fun geocodeAddress(addressText: String) {
        if (addressText.isBlank()) return
        viewModelScope.launch {
            _isGeocoding.value = true
            geocodingHelper.geocodeAddress(addressText)
                .onSuccess { coords ->
                    _geocodedLocation.value = coords.latitude to coords.longitude
                    _message.value = "Endereço localizado no mapa"
                }
                .onFailure { e ->
                    _message.value = "Não foi possível localizar o endereço: ${e.message}"
                }
            _isGeocoding.value = false
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation() {
        viewModelScope.launch {
            runCatching { fusedLocationProviderClient.lastLocation.await() }
                .onSuccess { location ->
                    if (location != null) {
                        _currentGpsLocation.value = location.latitude to location.longitude
                    } else {
                        _message.value = "Não foi possível obter a localização atual"
                    }
                }
                .onFailure { e ->
                    _message.value = "Erro ao obter localização: ${e.message}"
                }
        }
    }

    fun startEditing(address: WorkAddress?) {
        clearDialogState()
        _editingAddress.value = address ?: WorkAddress(
            name = "",
            addressText = "",
            latitude = 0.0,
            longitude = 0.0,
            radius = 50f
        )
    }

    fun stopEditing() {
        clearDialogState()
    }

    fun deleteAddress(address: WorkAddress) {
        viewModelScope.launch {
            repository.deleteAddress(address)
            syncGeofences()
            _message.value = "Local removido"
        }
    }

    fun toggleActive(address: WorkAddress) {
        viewModelScope.launch {
            repository.updateAddress(address.copy(isActive = !address.isActive))
            syncGeofences()
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun consumeGeocodedLocation() {
        _geocodedLocation.value = null
    }

    fun consumeCurrentGpsLocation() {
        _currentGpsLocation.value = null
    }

    private fun clearDialogState() {
        _editingAddress.value = null
        _geocodedLocation.value = null
        _currentGpsLocation.value = null
    }
}

private const val STOP_TIMEOUT_MS = 5000L
