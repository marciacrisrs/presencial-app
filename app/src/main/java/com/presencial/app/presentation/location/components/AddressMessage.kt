package com.presencial.app.presentation.location.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AddressMessage(
    state: com.presencial.app.presentation.location.WorkAddressDialogState
) {
    when {
        !state.hasValidCoordinates && state.permissionsGranted -> {
            Text(
                text = "Toque no mapa, busque o endereço ou use sua localização atual.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        !state.hasValidCoordinates && !state.permissionsGranted -> {
            Text(
                text = "Conceda permissão de localização ou busque o endereço no mapa.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        else -> {
            Text(
                text = "Coordenadas: ${"%.5f".format(state.latitude)}, ${"%.5f".format(state.longitude)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
