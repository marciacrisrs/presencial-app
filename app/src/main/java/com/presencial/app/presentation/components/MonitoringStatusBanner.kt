package com.presencial.app.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MonitoringStatusBanner(
    activeAddressCount: Int,
    foregroundGranted: Boolean,
    backgroundGranted: Boolean,
    modifier: Modifier = Modifier
) {
    if (activeAddressCount == 0) return

    val isFullyConfigured = foregroundGranted && backgroundGranted
    val containerColor = if (isFullyConfigured) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isFullyConfigured) {
                    "Monitoramento ativo ($activeAddressCount local${if (activeAddressCount > 1) "is" else ""})"
                } else {
                    "Check-in automático incompleto"
                },
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = when {
                    isFullyConfigured ->
                        "O app pode registrar sua presença ao permanecer no raio configurado."
                    !foregroundGranted ->
                        "Conceda permissão de localização para ativar o monitoramento."
                    else ->
                        "Permita localização \"O tempo todo\" para check-in em background."
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
