package com.presencial.app.presentation.history

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.presencial.app.R
import com.presencial.app.domain.model.HistoryMonthData
import com.presencial.app.domain.model.MonthlySummary
import com.presencial.app.domain.model.WeeklyPolicySummary
import com.presencial.app.ui.components.ShimmerBox
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val historyMonths by viewModel.historyMonths.collectAsStateWithLifecycle()
    val weeklySummaries by viewModel.weeklySummaries.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showWeeklySummary by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Histórico", style = MaterialTheme.typography.headlineLarge)

        Button(
            onClick = { showWeeklySummary = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = weeklySummaries.isNotEmpty()
        ) {
            Text(stringResource(R.string.policy_weekly_summary_title))
        }

        if (historyMonths.isEmpty()) {
            HistorySkeleton()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(
                    items = historyMonths.sortedByDescending { it.summary.yearMonth },
                    key = { _, item -> item.summary.yearMonth }
                ) { index, monthData ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(ANIM_DURATION, index * ANIM_DELAY)) +
                                slideInVertically(
                                    animationSpec = tween(ANIM_DURATION, index * ANIM_DELAY)
                                ) { it / 2 }
                    ) {
                        HistoryMonthCard(
                            monthData = monthData,
                            onShare = {
                                shareSummary(context, monthData.summary)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showWeeklySummary) {
        WeeklySummaryDialog(
            summaries = weeklySummaries,
            onDismiss = { showWeeklySummary = false }
        )
    }
}

@Composable
private fun WeeklySummaryDialog(
    summaries: List<WeeklyPolicySummary>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.policy_weekly_summary_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                summaries.forEach { week ->
                    val mode = stringResource(
                        if (week.isOnSiteWeek) R.string.policy_week_on_site else R.string.policy_week_remote
                    )
                    Text(
                        stringResource(
                            R.string.policy_weekly_summary_line,
                            week.weekStart.dayOfMonth,
                            week.weekStart.monthValue,
                            week.weekEnd.dayOfMonth,
                            week.weekEnd.monthValue,
                            mode,
                            week.requiredCount
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

private fun shareSummary(context: android.content.Context, summary: MonthlySummary) {
    val monthName = summary.yearMonth.month.getDisplayName(
        TextStyle.FULL,
        Locale.forLanguageTag("pt-BR")
    )
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
    context.startActivity(
        Intent.createChooser(intent, "Compartilhar resumo")
    )
}

@Composable
fun HistorySkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(SKELETON_COUNT) {
            ShimmerBox(height = 140.dp, shape = RoundedCornerShape(20.dp))
        }
    }
}

private const val ANIM_DURATION = 500
private const val ANIM_DELAY = 100
private const val SKELETON_COUNT = 3

@Composable
private fun HistoryMonthCard(monthData: HistoryMonthData, onShare: () -> Unit) {
    val summary = monthData.summary
    val monthName = summary.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"))
    val progress = if (summary.requiredDays > 0) {
        summary.completedDays.toFloat() / summary.requiredDays
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
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
            if (monthData.autoCheckInDays > 0) {
                Text(
                    "📍 ${monthData.autoCheckInDays} check-in(s) automático(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
