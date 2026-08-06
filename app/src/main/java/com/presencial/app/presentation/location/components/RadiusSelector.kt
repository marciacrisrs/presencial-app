package com.presencial.app.presentation.location.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun RadiusSelector(
    radius: Float,
    onRadiusChanged: (Float) -> Unit
) {

    Text(
        "Raio de Ativação: ${radius.toInt()} metros",
        style = MaterialTheme.typography.labelMedium
    )

    Slider(
        value = radius,
        onValueChange = onRadiusChanged,
        valueRange = 50f..500f,
        steps = 9
    )

}