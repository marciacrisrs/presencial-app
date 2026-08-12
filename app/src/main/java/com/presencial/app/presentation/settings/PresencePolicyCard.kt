package com.presencial.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.presencial.app.R
import com.presencial.app.domain.model.PolicyValidationResult
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.model.WeekParity
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresencePolicyCard(
    policy: PresencePolicy,
    validation: PolicyValidationResult,
    onPolicyChange: (PresencePolicy) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.policy_card_title), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.policy_card_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            PercentageSection(
                policy = policy,
                onPolicyChange = onPolicyChange
            )

            PolicyToggleSection(
                title = stringResource(R.string.policy_fixed_weekdays_title),
                description = stringResource(R.string.policy_fixed_weekdays_description),
                checked = policy.fixedWeekdaysEnabled,
                onCheckedChange = { enabled ->
                    val weekdays = if (enabled && policy.mandatoryWeekdays.isEmpty()) {
                        setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
                    } else {
                        policy.mandatoryWeekdays
                    }
                    onPolicyChange(
                        policy.copy(
                            fixedWeekdaysEnabled = enabled,
                            mandatoryWeekdays = weekdays
                        )
                    )
                }
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    weekdayOptions.forEach { (day, label) ->
                        FilterChip(
                            selected = day in policy.mandatoryWeekdays,
                            onClick = {
                                val updated = policy.mandatoryWeekdays.toMutableSet()
                                if (day in updated) updated.remove(day) else updated.add(day)
                                onPolicyChange(policy.copy(mandatoryWeekdays = updated))
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }

            PolicyToggleSection(
                title = stringResource(R.string.policy_alternating_weeks_title),
                description = stringResource(R.string.policy_alternating_weeks_description),
                checked = policy.alternatingWeeksEnabled,
                onCheckedChange = { onPolicyChange(policy.copy(alternatingWeeksEnabled = it)) }
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = policy.onSiteWeekParity == WeekParity.EVEN,
                        onClick = { onPolicyChange(policy.copy(onSiteWeekParity = WeekParity.EVEN)) },
                        label = { Text(stringResource(R.string.policy_even_weeks_on_site)) }
                    )
                    FilterChip(
                        selected = policy.onSiteWeekParity == WeekParity.ODD,
                        onClick = { onPolicyChange(policy.copy(onSiteWeekParity = WeekParity.ODD)) },
                        label = { Text(stringResource(R.string.policy_odd_weeks_on_site)) }
                    )
                }
            }

            validation.errors.forEach {
                Text(
                    stringResource(R.string.policy_validation_error, it),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            validation.warnings.forEach {
                Text(
                    stringResource(R.string.policy_validation_warning, it),
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PercentageSection(
    policy: PresencePolicy,
    onPolicyChange: (PresencePolicy) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.policy_free_percentage_title),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    stringResource(R.string.policy_free_percentage_description),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = policy.freePercentageEnabled,
                onCheckedChange = { onPolicyChange(policy.copy(freePercentageEnabled = it)) }
            )
        }

        if (policy.freePercentageEnabled) {
            Text(
                text = "${policy.freePercentage}%",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = policy.freePercentage.toFloat(),
                onValueChange = { value ->
                    onPolicyChange(policy.copy(freePercentage = value.toInt()))
                },
                valueRange = PresencePolicy.MIN_PERCENTAGE.toFloat()..PresencePolicy.MAX_PERCENTAGE.toFloat(),
                steps = 98,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(20, 40, 60, 80).forEach { pct ->
                    FilterChip(
                        selected = policy.freePercentage == pct,
                        onClick = { onPolicyChange(policy.copy(freePercentage = pct)) },
                        label = { Text("$pct%") }
                    )
                }
            }
        }
    }
}

@Composable
private fun PolicyToggleSection(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
        if (checked) content()
    }
}

private val weekdayOptions = listOf(
    DayOfWeek.MONDAY to DayOfWeek.MONDAY.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR")),
    DayOfWeek.TUESDAY to DayOfWeek.TUESDAY.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR")),
    DayOfWeek.WEDNESDAY to DayOfWeek.WEDNESDAY.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR")),
    DayOfWeek.THURSDAY to DayOfWeek.THURSDAY.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR")),
    DayOfWeek.FRIDAY to DayOfWeek.FRIDAY.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR")),
    DayOfWeek.SATURDAY to DayOfWeek.SATURDAY.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR"))
)
