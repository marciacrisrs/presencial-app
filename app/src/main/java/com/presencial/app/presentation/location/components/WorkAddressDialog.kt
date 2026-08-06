package com.presencial.app.presentation.location.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import com.presencial.app.domain.model.WorkAddress
import com.presencial.app.presentation.location.WorkAddressDialogState

@Composable
internal fun WorkAddressDialog(
    address: WorkAddress?,
    permissionsGranted: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Float, Boolean) -> Unit
) {

    val isNewAddress = address == null || address.id == 0L

    var name by remember {
        mutableStateOf(address?.name.orEmpty())
    }

    var addressText by remember {
        mutableStateOf(address?.addressText.orEmpty())
    }

    var radius by remember {
        mutableStateOf(address?.radius ?: 50f)
    }

    val state = remember(
        name,
        addressText,
        radius,
        isNewAddress,
        permissionsGranted
    ) {
        WorkAddressDialogState(
            name = name,
            addressText = addressText,
            radius = radius,
            isNewAddress = isNewAddress,
            permissionsGranted = permissionsGranted
        )
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            DialogTitle(state)
        },

        text = {

            DialogContent(
                state = state,
                onNameChanged = { name = it },
                onAddressChanged = { addressText = it },
                onRadiusChanged = { radius = it }
            )

        },

        confirmButton = {

            ConfirmButton(
                state = state,
                enabled = name.isNotBlank() &&
                        (permissionsGranted || !isNewAddress)
            ) {

                onConfirm(
                    name,
                    addressText,
                    radius,
                    isNewAddress
                )

            }

        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}