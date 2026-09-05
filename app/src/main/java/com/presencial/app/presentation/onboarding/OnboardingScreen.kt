package com.presencial.app.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
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
        onFinish = viewModel::complete
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
    onFinish: () -> Unit
) {
    val currentStep = OnboardingEligibility.coerceStep(step)
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val compact = maxHeight < COMPACT_HEIGHT
        val horizontalPadding = if (maxWidth < NARROW_WIDTH) PADDING_COMPACT else PADDING_COMFORTABLE
        val verticalPadding = if (compact) PADDING_COMPACT else PADDING_COMFORTABLE
        val titleStyle = if (compact) {
            MaterialTheme.typography.headlineMedium
        } else {
            MaterialTheme.typography.headlineLarge
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (compact) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
        ) {
            Text(
                text = stringResource(R.string.onboarding_title),
                style = titleStyle
            )
            Spacer(Modifier.height(PROGRESS_TOP_SPACING))
            LinearProgressIndicator(
                progress = { (currentStep + 1f) / OnboardingEligibility.STEP_COUNT },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PROGRESS_HEIGHT),
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(STEP_INDICATOR_SPACING))
            Text(
                text = stringResource(
                    R.string.onboarding_step_indicator,
                    currentStep + 1,
                    OnboardingEligibility.STEP_COUNT
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = SECONDARY_ALPHA)
            )

            if (compact) {
                Spacer(Modifier.height(CONTENT_TOP_SPACING_COMPACT))
                StepBody(
                    step = currentStep,
                    selectedPercentage = selectedPercentage,
                    onSelectPercentage = onSelectPercentage,
                    compact = true
                )
                Spacer(Modifier.height(ACTIONS_TOP_SPACING))
                StepActions(
                    step = currentStep,
                    onContinueGoal = onContinueGoal,
                    onContinueReminder = onContinueReminder,
                    onAddWorkAddress = onAddWorkAddress,
                    onFinish = onFinish
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(BODY_SPACING, Alignment.CenterVertically)
                ) {
                    StepBody(
                        step = currentStep,
                        selectedPercentage = selectedPercentage,
                        onSelectPercentage = onSelectPercentage,
                        compact = false
                    )
                }
                StepActions(
                    step = currentStep,
                    onContinueGoal = onContinueGoal,
                    onContinueReminder = onContinueReminder,
                    onAddWorkAddress = onAddWorkAddress,
                    onFinish = onFinish
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.StepBody(
    step: Int,
    selectedPercentage: Int,
    onSelectPercentage: (Int) -> Unit,
    compact: Boolean
) {
    val titleStyle = if (compact) {
        MaterialTheme.typography.titleLarge
    } else {
        MaterialTheme.typography.headlineMedium
    }
    val bodyStyle = if (compact) {
        MaterialTheme.typography.bodyMedium
    } else {
        MaterialTheme.typography.bodyLarge
    }
    when (step) {
        OnboardingEligibility.STEP_GOAL -> {
            Text(stringResource(R.string.onboarding_goal_title), style = titleStyle)
            Text(stringResource(R.string.onboarding_goal_body), style = bodyStyle)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(CHIP_SPACING)) {
                GOAL_PRESETS.forEach { percentage ->
                    FilterChip(
                        selected = selectedPercentage == percentage,
                        onClick = { onSelectPercentage(percentage) },
                        modifier = Modifier.height(if (compact) CHIP_HEIGHT_COMPACT else CHIP_HEIGHT),
                        label = { Text(stringResource(R.string.onboarding_goal_percent, percentage)) }
                    )
                }
            }
        }
        OnboardingEligibility.STEP_REMINDER -> {
            Text(stringResource(R.string.onboarding_reminder_title), style = titleStyle)
            Text(stringResource(R.string.onboarding_reminder_body), style = bodyStyle)
        }
        else -> {
            Text(stringResource(R.string.onboarding_location_title), style = titleStyle)
            Text(stringResource(R.string.onboarding_location_body), style = bodyStyle)
        }
    }
}

@Composable
private fun ColumnScope.StepActions(
    step: Int,
    onContinueGoal: () -> Unit,
    onContinueReminder: () -> Unit,
    onAddWorkAddress: () -> Unit,
    onFinish: () -> Unit
) {
    val buttonModifier = Modifier
        .fillMaxWidth()
        .height(ACTION_BUTTON_HEIGHT)
    when (step) {
        OnboardingEligibility.STEP_GOAL -> {
            Button(onClick = onContinueGoal, modifier = buttonModifier) {
                Text(stringResource(R.string.onboarding_continue))
            }
        }
        OnboardingEligibility.STEP_REMINDER -> {
            Button(onClick = onContinueReminder, modifier = buttonModifier) {
                Text(stringResource(R.string.onboarding_continue))
            }
        }
        else -> {
            Button(onClick = onAddWorkAddress, modifier = buttonModifier) {
                Text(stringResource(R.string.onboarding_location_add))
            }
        }
    }
    TextButton(onClick = onFinish, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.onboarding_finish))
    }
}

private val GOAL_PRESETS = listOf(20, 40, 60, 80)
private val COMPACT_HEIGHT = 560.dp
private val NARROW_WIDTH = 360.dp
private val PADDING_COMPACT = 20.dp
private val PADDING_COMFORTABLE = 32.dp
private val PROGRESS_TOP_SPACING = 16.dp
private val PROGRESS_HEIGHT = 6.dp
private val STEP_INDICATOR_SPACING = 12.dp
private val CONTENT_TOP_SPACING_COMPACT = 20.dp
private val ACTIONS_TOP_SPACING = 24.dp
private val BODY_SPACING = 16.dp
private val CHIP_SPACING = 8.dp
private val CHIP_HEIGHT = 48.dp
private val CHIP_HEIGHT_COMPACT = 40.dp
private val ACTION_BUTTON_HEIGHT = 56.dp
private const val SECONDARY_ALPHA = 0.7f
