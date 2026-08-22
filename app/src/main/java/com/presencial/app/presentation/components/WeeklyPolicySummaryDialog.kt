package com.presencial.app.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.presencial.app.R
import com.presencial.app.domain.model.WeeklyPolicySummary

@Composable
fun WeeklyPolicySummaryDialog(
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
