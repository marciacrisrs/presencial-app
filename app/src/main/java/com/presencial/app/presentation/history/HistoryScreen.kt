package com.presencial.app.presentation.history

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.presencial.app.domain.model.MonthlySummary
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val summaries by viewModel.summaries.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Histórico", style = MaterialTheme.typography.headlineLarge)

        if (summaries.isEmpty()) {
            Text(
                "Nenhum mês registrado ainda.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(summaries.sortedByDescending { it.yearMonth }, key = { it.yearMonth }) { summary ->
                    HistoryMonthCard(
                        summary = summary,
                        onShare = {
                            val monthName = summary.yearMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
                            val text = buildString {
                                append("📊 Presencial — $monthName ${summary.yearMonth.year}\n")
                                append("Dias úteis: ${summary.workdays}\n")
                                append("Meta: ${summary.requiredDays} dias (${summary.requiredPercentage}%)\n")
                                append("Cumpridos: ${summary.completedDays}\n")
                                append("Percentual: ${"%.1f".format(summary.achievedPercentage)}%")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            ContextCompat.startActivity(
                                context,
                                Intent.createChooser(intent, "Compartilhar resumo"),
                                null
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryMonthCard(summary: MonthlySummary, onShare: () -> Unit) {
    val monthName = summary.yearMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
    val progress = if (summary.requiredDays > 0) {
        summary.completedDays.toFloat() / summary.requiredDays
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${monthName.replaceFirstChar { it.uppercase() }} ${summary.yearMonth.year}",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Compartilhar")
                }
            }
            Text("Dias úteis: ${summary.workdays}  •  Meta: ${summary.requiredDays} dias")
            Text("Cumpridos: ${summary.completedDays}  •  ${"%.0f".format(summary.achievedPercentage)}%")
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
