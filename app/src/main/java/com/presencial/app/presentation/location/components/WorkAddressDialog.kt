package com.presencial.app.presentation.location.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    onDismiss: () -> Unit,
    onConfirm: (WorkAddressDialogResult) -> Unit
) {

    val isNewAddress = address == null || address.id == 0L

    var name by remember {
        mutableStateOf(address?.name.orEmpty())
    }

    var addressText by remember {
        mutableStateOf(address?.addressText.orEmpty())
    }

    var radius by remember {
        mutableFloatStateOf(address?.radius ?: DEFAULT_RADIUS)
    }

    val state = rememberWorkAddressDialogState(
        name = name,
        addressText = addressText,
        radius = radius,
        isNewAddress = isNewAddress,
        permissionsGranted = permissionsGranted
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(state) },
        text = {
            DialogContent(
                state = state,
                onNameChanged = { name = it },
                onAddressChanged = { addressText = it },
                onRadiusChanged = { radius = it }
            )
        },
        confirmButton = {
            WorkAddressConfirmButton(
                state = state,
                onConfirm = {
                    onConfirm(
                        WorkAddressDialogResult(
                            name = name,
                            addressText = addressText,
                            radius = radius,
                            isNew = isNewAddress
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
private fun rememberWorkAddressDialogState(
    name: String,
    addressText: String,
    radius: Float,
    isNewAddress: Boolean,
    permissionsGranted: Boolean
) = remember(name, addressText, radius, isNewAddress, permissionsGranted) {
    WorkAddressDialogState(
        name = name,
        addressText = addressText,
        radius = radius,
        isNewAddress = isNewAddress,
        permissionsGranted = permissionsGranted
    )
}

data class WorkAddressDialogResult(
    val name: String,
    val addressText: String,
    val radius: Float,
    val isNew: Boolean
)

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

private const val DEFAULT_RADIUS = 50f

