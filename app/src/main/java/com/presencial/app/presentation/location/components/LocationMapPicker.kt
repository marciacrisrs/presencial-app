package com.presencial.app.presentation.location.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LocationMapPicker(
    latitude: Double,
    longitude: Double,
    @Suppress("UNUSED_PARAMETER") onLocationChanged: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasValidCoords = latitude != 0.0 || longitude != 0.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Localização geográfica",
                style = MaterialTheme.typography.labelLarge
            )
            if (hasValidCoords) {
                Text(
                    text = "Latitude: ${"%.5f".format(latitude)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Longitude: ${"%.5f".format(longitude)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "O check-in automático usa este ponto como centro do raio.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text(
                    text = "Busque o endereço ou use sua localização atual para definir o ponto.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
