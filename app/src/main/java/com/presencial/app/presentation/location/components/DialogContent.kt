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
    onRadiusChanged: (Float) -> Unit
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        NameField(
            state.name,
            onNameChanged
        )

        AddressField(
            state.address,
            onAddressChanged
        )

        RadiusSelector(
            state.radius,
            onRadiusChanged
        )

        AddressMessage(state)
    }
}
