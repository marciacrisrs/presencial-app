package com.presencial.app.presentation.location.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier

@Composable
fun AddressField(
    value: String,
    onValueChange: (String) -> Unit,
    onGeocodeClick: () -> Unit,
    isGeocoding: Boolean,
    geocodeEnabled: Boolean
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text("Endereço") },
        trailingIcon = {
            TextButton(
                onClick = onGeocodeClick,
                enabled = geocodeEnabled && value.isNotBlank() && !isGeocoding
            ) {
                Text(if (isGeocoding) "..." else "Buscar")
            }
        }
    )
}
