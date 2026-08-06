package com.presencial.app.presentation.location.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.presencial.app.presentation.location.WorkAddressDialogState

@Composable
internal fun ConfirmButton(
    state: WorkAddressDialogState,
    enabled: Boolean,
    onClick: () -> Unit
) {

    Button(
        enabled = enabled,
        onClick = onClick
    ) {

        Text(
            if (state.isNewAddress)
                "Salvar Local Atual"
            else
                "Atualizar Local"
        )

    }

}