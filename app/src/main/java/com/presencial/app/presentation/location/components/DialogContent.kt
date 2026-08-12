package com.presencial.app.presentation.location.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.presencial.app.presentation.location.WorkAddressDialogState

@Composable
fun DialogContent(
    state: WorkAddressDialogState,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onRadiusChanged: (Float) -> Unit,
    onLocationChanged: (Double, Double) -> Unit,
    onGeocodeClick: () -> Unit,
    onUseCurrentLocation: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NameField(
            state.name,
            onNameChanged
        )

        AddressField(
            value = state.address,
            onValueChange = onAddressChanged,
            onGeocodeClick = onGeocodeClick,
            isGeocoding = state.isGeocoding,
            geocodeEnabled = state.address.isNotBlank()
        )

        LocationMapPicker(
            latitude = state.latitude,
            longitude = state.longitude,
            onLocationChanged = onLocationChanged
        )

        if (state.permissionsGranted) {
            androidx.compose.material3.TextButton(onClick = onUseCurrentLocation) {
                androidx.compose.material3.Text("Usar minha localização atual")
            }
        }

        RadiusSelector(
            state.radius,
            onRadiusChanged
        )

        AddressMessage(state)
    }
}
