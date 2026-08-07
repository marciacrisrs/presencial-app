package com.presencial.app.presentation.location.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.presencial.app.presentation.location.WorkAddressDialogState

@Composable
fun DialogTitle(
    state: WorkAddressDialogState
) {
    Text(state.dialogTitle)
}
