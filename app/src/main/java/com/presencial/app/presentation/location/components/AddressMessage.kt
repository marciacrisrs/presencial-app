package com.presencial.app.presentation.location.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AddressMessage(
    isNewAddress: Boolean,
    permissionsGranted: Boolean
) {
    when {
        isNewAddress && permissionsGranted -> {
            Text(
                text = "O local será definido com base na sua posição atual.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        isNewAddress && !permissionsGranted -> {
            Text(
                text = "Conceda permissão de localização para salvar este local.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        else -> {
            Text(
                text = "Localização atualizada automaticamente se salvar agora.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}