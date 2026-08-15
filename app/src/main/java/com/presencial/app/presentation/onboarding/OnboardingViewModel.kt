package com.presencial.app.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.OnboardingEligibility
import com.presencial.app.domain.widget.WidgetRefresher
import com.presencial.app.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val visible: Boolean,
    val step: Int,
    val selectedPercentage: Int,
    val policy: PresencePolicy
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    checkInRepository: CheckInRepository,
    private val notificationScheduler: NotificationScheduler,
    private val widgetRefresher: WidgetRefresher
) : ViewModel() {

    val uiState: StateFlow<OnboardingUiState?> = combine(
        settingsRepository.settings,
        checkInRepository.observeAllCheckIns()
    ) { appSettings, checkIns ->
        OnboardingUiState(
            visible = OnboardingEligibility.shouldShow(
                appSettings.onboardingCompleted,
                checkIns.isNotEmpty()
            ),
            step = appSettings.onboardingStep,
            selectedPercentage = appSettings.requiredPercentage,
            policy = appSettings.presencePolicy
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.settings,
                checkInRepository.observeAllCheckIns()
            ) { appSettings, checkIns ->
                appSettings.onboardingCompleted to checkIns.isNotEmpty()
            }.collect { (completed, hasCheckIns) ->
                if (!completed && hasCheckIns) {
                    settingsRepository.completeOnboarding()
                }
            }
        }
    }

    fun selectPercentage(percentage: Int) {
        viewModelScope.launch {
            val current = uiState.value?.policy ?: PresencePolicy.fromLegacyPercentage(percentage)
            settingsRepository.updatePresencePolicy(
                current.copy(freePercentageEnabled = false, freePercentage = percentage)
            )
            widgetRefresher.refresh()
        }
    }

    fun continueFromGoal() {
        viewModelScope.launch {
            settingsRepository.updateOnboardingStep(OnboardingEligibility.STEP_REMINDER)
        }
    }

    fun continueFromReminder() {
        notificationScheduler.scheduleDailyReminder()
        viewModelScope.launch {
            settingsRepository.updateOnboardingStep(OnboardingEligibility.STEP_LOCATION)
        }
    }

    fun complete() {
        viewModelScope.launch {
            settingsRepository.completeOnboarding()
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
