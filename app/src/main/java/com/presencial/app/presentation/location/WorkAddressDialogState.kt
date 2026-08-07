package com.presencial.app.presentation.location

import androidx.compose.runtime.Immutable

@Immutable
data class WorkAddressDialogState(
    val name: String,
    val addressText: String,
    val radius: Float,
    val isNewAddress: Boolean,
    val permissionsGranted: Boolean
) {

    val address: String get() = addressText
    val dialogTitle: String
        get() = if (isNewAddress) {
            "Novo Local"
        } else {
            "Editar Local"
        }

    val confirmButtonText: String
        get() = if (isNewAddress) {
            "Salvar Local Atual"
        } else {
            "Atualizar Local"
        }

    val canUseCurrentLocation: Boolean
        get() = isNewAddress

    fun canSave(name: String): Boolean {
        return name.isNotBlank() &&
                (permissionsGranted || !isNewAddress)
    }
}
