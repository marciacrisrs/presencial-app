package com.presencial.app.presentation.location.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.presencial.app.domain.model.WorkAddress
import com.presencial.app.presentation.location.WorkAddressDialogState

@Composable
internal fun WorkAddressDialog(
    address: WorkAddress?,
    permissionsGranted: Boolean,
    isGeocoding: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (WorkAddressDialogResult) -> Unit,
    onGeocodeRequest: (String) -> Unit,
    onUseCurrentLocation: () -> Unit,
    geocodedLocation: Pair<Double, Double>?,
    currentGpsLocation: Pair<Double, Double>?,
    onLocationConsumed: () -> Unit
) {
    val isNewAddress = address == null || address.id == 0L

    var name by remember(address?.id) {
        mutableStateOf(address?.name.orEmpty())
    }
    var addressText by remember(address?.id) {
        mutableStateOf(address?.addressText.orEmpty())
    }
    var radius by remember(address?.id) {
        mutableFloatStateOf(address?.radius ?: DEFAULT_RADIUS)
    }
    var latitude by remember(address?.id) {
        mutableDoubleStateOf(address?.latitude ?: 0.0)
    }
    var longitude by remember(address?.id) {
        mutableDoubleStateOf(address?.longitude ?: 0.0)
    }

    LaunchedEffect(geocodedLocation) {
        geocodedLocation?.let { (lat, lng) ->
            latitude = lat
            longitude = lng
            onLocationConsumed()
        }
    }

    LaunchedEffect(currentGpsLocation) {
        currentGpsLocation?.let { (lat, lng) ->
            latitude = lat
            longitude = lng
            onLocationConsumed()
        }
    }

    val state = remember(
        name,
        addressText,
        radius,
        latitude,
        longitude,
        isNewAddress,
        permissionsGranted,
        isGeocoding
    ) {
        WorkAddressDialogState(
            name = name,
            addressText = addressText,
            radius = radius,
            latitude = latitude,
            longitude = longitude,
            isNewAddress = isNewAddress,
            permissionsGranted = permissionsGranted,
            isGeocoding = isGeocoding
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(state) },
        text = {
            DialogContent(
                state = state,
                onNameChanged = { name = it },
                onAddressChanged = { addressText = it },
                onRadiusChanged = { radius = it },
                onLocationChanged = { lat, lng ->
                    latitude = lat
                    longitude = lng
                },
                onGeocodeClick = { onGeocodeRequest(addressText) },
                onUseCurrentLocation = onUseCurrentLocation
            )
        },
        confirmButton = {
            WorkAddressConfirmButton(
                state = state,
                onConfirm = {
                    onConfirm(
                        WorkAddressDialogResult(
                            id = address?.id ?: 0L,
                            name = name,
                            addressText = addressText,
                            radius = radius,
                            latitude = latitude,
                            longitude = longitude,
                            isActive = address?.isActive ?: true
                        )
                    )
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun WorkAddressConfirmButton(
    state: WorkAddressDialogState,
    onConfirm: () -> Unit
) {
    ConfirmButton(
        state = state,
        enabled = state.canSave(state.name)
    ) {
        onConfirm()
    }
}

data class WorkAddressDialogResult(
    val id: Long,
    val name: String,
    val addressText: String,
    val radius: Float,
    val latitude: Double,
    val longitude: Double,
    val isActive: Boolean
)

private const val DEFAULT_RADIUS = 50f
