package com.presencial.app.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.presencial.app.R
import com.presencial.app.domain.util.OnboardingEligibility
import com.presencial.app.presentation.notification.RequestNotificationPermissionOnLaunch

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onAddWorkAddress: () -> Unit
) {
    RequestNotificationPermissionOnLaunch()
    val settings by viewModel.uiState.collectAsStateWithLifecycle()
    val state = settings ?: return
    OnboardingContent(
        step = state.step,
        selectedPercentage = state.selectedPercentage,
        onSelectPercentage = viewModel::selectPercentage,
        onContinueGoal = viewModel::continueFromGoal,
        onContinueReminder = viewModel::continueFromReminder,
        onAddWorkAddress = onAddWorkAddress,
        onSkipLocation = viewModel::complete
    )
}

@Composable
fun OnboardingContent(
    step: Int,
    selectedPercentage: Int,
    onSelectPercentage: (Int) -> Unit,
    onContinueGoal: () -> Unit,
    onContinueReminder: () -> Unit,
    onAddWorkAddress: () -> Unit,
    onSkipLocation: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SCREEN_PADDING),
        verticalArrangement = Arrangement.spacedBy(CONTENT_SPACING)
    ) {
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = stringResource(
                R.string.onboarding_step_indicator,
                step + 1,
                OnboardingEligibility.STEP_COUNT
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = SECONDARY_ALPHA)
        )
        when (OnboardingEligibility.coerceStep(step)) {
            OnboardingEligibility.STEP_GOAL -> GoalStep(
                selectedPercentage = selectedPercentage,
                onSelectPercentage = onSelectPercentage,
                onContinue = onContinueGoal
            )
            OnboardingEligibility.STEP_REMINDER -> ReminderStep(onContinue = onContinueReminder)
            else -> LocationStep(
                onAddWorkAddress = onAddWorkAddress,
                onSkip = onSkipLocation
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoalStep(
    selectedPercentage: Int,
    onSelectPercentage: (Int) -> Unit,
    onContinue: () -> Unit
) {
    Text(stringResource(R.string.onboarding_goal_title), style = MaterialTheme.typography.titleLarge)
    Text(
        stringResource(R.string.onboarding_goal_body),
        style = MaterialTheme.typography.bodyMedium
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(CHIP_SPACING)) {
        GOAL_PRESETS.forEach { percentage ->
            FilterChip(
                selected = selectedPercentage == percentage,
                onClick = { onSelectPercentage(percentage) },
                label = { Text(stringResource(R.string.onboarding_goal_percent, percentage)) }
            )
        }
    }
    Spacer(Modifier.height(BUTTON_SPACER))
    Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.onboarding_continue))
    }
}

@Composable
private fun ReminderStep(onContinue: () -> Unit) {
    Text(stringResource(R.string.onboarding_reminder_title), style = MaterialTheme.typography.titleLarge)
    Text(
        stringResource(R.string.onboarding_reminder_body),
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(Modifier.height(BUTTON_SPACER))
    Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.onboarding_continue))
    }
}

@Composable
private fun LocationStep(
    onAddWorkAddress: () -> Unit,
    onSkip: () -> Unit
) {
    Text(stringResource(R.string.onboarding_location_title), style = MaterialTheme.typography.titleLarge)
    Text(
        stringResource(R.string.onboarding_location_body),
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(Modifier.height(BUTTON_SPACER))
    Button(onClick = onAddWorkAddress, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.onboarding_location_add))
    }
    TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.onboarding_finish))
    }
}

private val GOAL_PRESETS = listOf(20, 40, 60, 80)
private val SCREEN_PADDING = 24.dp
private val CONTENT_SPACING = 12.dp
private val CHIP_SPACING = 8.dp
private val BUTTON_SPACER = 16.dp
private const val SECONDARY_ALPHA = 0.7f
