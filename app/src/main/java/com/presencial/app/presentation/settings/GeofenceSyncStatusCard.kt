package com.presencial.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.presencial.app.domain.model.GeofenceSyncStatus

@Composable
fun GeofenceSyncStatusCard(
    status: GeofenceSyncStatus,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val failure = status as? GeofenceSyncStatus.Failure ?: return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Monitoramento de localização com problema",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = failure.message,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onRetry) {
                    Text("Tentar novamente")
                }
            }
        }
    }
}
