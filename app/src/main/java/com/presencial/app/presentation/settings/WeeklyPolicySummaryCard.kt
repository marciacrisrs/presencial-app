package com.presencial.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.presencial.app.domain.model.WeeklyPolicySummary
import com.presencial.app.presentation.components.WeeklyPolicySummaryDialog

@Composable
fun WeeklyPolicySummaryCard(summaries: List<WeeklyPolicySummary>) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Resumo da política", style = MaterialTheme.typography.titleLarge)
            Text(
                "Veja como sua política de presença se aplica às semanas do mês.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = summaries.isNotEmpty()
            ) {
                Text("Ver resumo semanal")
            }
        }
    }

    if (showDialog) {
        WeeklyPolicySummaryDialog(
            summaries = summaries,
            onDismiss = { showDialog = false }
        )
    }
}
