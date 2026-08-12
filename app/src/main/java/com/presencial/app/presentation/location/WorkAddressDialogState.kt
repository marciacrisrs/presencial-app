package com.presencial.app.presentation.location

import androidx.compose.runtime.Immutable

@Immutable
data class WorkAddressDialogState(
    val name: String,
    val addressText: String,
    val radius: Float,
    val latitude: Double,
    val longitude: Double,
    val isNewAddress: Boolean,
    val permissionsGranted: Boolean,
    val isGeocoding: Boolean = false
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
            "Salvar Local"
        } else {
            "Atualizar Local"
        }

    val hasValidCoordinates: Boolean
        get() = latitude != 0.0 || longitude != 0.0

    fun canSave(name: String): Boolean {
        return name.isNotBlank() && hasValidCoordinates
    }
}
