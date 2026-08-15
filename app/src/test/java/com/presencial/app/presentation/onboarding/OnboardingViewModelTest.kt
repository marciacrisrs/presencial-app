package com.presencial.app.presentation.onboarding

import app.cash.turbine.test
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.CheckIn
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.repository.CheckInRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.OnboardingEligibility
import com.presencial.app.domain.widget.WidgetRefresher
import com.presencial.app.notification.NotificationScheduler
import com.presencial.app.util.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.LocalDate

class OnboardingViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val settingsFlow = MutableStateFlow(AppSettings())
    private val checkInsFlow = MutableStateFlow(emptyList<CheckIn>())
    private val settingsRepository = mockk<SettingsRepository>()
    private val checkInRepository = mockk<CheckInRepository>()
    private val notificationScheduler = mockk<NotificationScheduler>(relaxed = true)
    private val widgetRefresher = mockk<WidgetRefresher>(relaxed = true)

    @BeforeEach
    fun setup() {
        every { settingsRepository.settings } returns settingsFlow
        every { checkInRepository.observeAllCheckIns() } returns checkInsFlow
        coEvery { settingsRepository.updatePresencePolicy(any()) } returns Unit
        coEvery { settingsRepository.updateOnboardingStep(any()) } returns Unit
        coEvery { settingsRepository.completeOnboarding() } returns Unit
        coEvery { widgetRefresher.refresh() } returns Unit
    }

    private fun createViewModel() = OnboardingViewModel(
        settingsRepository,
        checkInRepository,
        notificationScheduler,
        widgetRefresher
    )

    private suspend fun app.cash.turbine.ReceiveTurbine<OnboardingUiState?>.awaitLoaded(): OnboardingUiState {
        val first = awaitItem()
        return first ?: requireNotNull(awaitItem())
    }

    @Test
    fun `primeiro launch mostra onboarding`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.test {
            val state = awaitLoaded()
            assertEquals(true, state.visible)
            assertEquals(OnboardingEligibility.STEP_GOAL, state.step)
        }
    }

    @Test
    fun `onboarding concluido nao reaparece`() = runTest {
        settingsFlow.value = AppSettings(onboardingCompleted = true)
        val viewModel = createViewModel()
        viewModel.uiState.test {
            assertEquals(false, awaitLoaded().visible)
        }
    }

    @Test
    fun `check-ins existentes marcam onboarding como concluido`() = runTest {
        checkInsFlow.value = listOf(
            CheckIn(date = LocalDate.of(2026, 8, 1), status = DayStatus.PRESENCIAL)
        )
        val viewModel = createViewModel()
        viewModel.uiState.test {
            awaitLoaded()
        }
        coVerify { settingsRepository.completeOnboarding() }
    }

    @Test
    fun `onboarding interrompido retoma o passo persistido`() = runTest {
        settingsFlow.value = AppSettings(onboardingCompleted = false, onboardingStep = 1)
        val viewModel = createViewModel()
        viewModel.uiState.test {
            val state = awaitLoaded()
            assertEquals(true, state.visible)
            assertEquals(OnboardingEligibility.STEP_REMINDER, state.step)
        }
    }

    @Test
    fun `continuar da meta avanca para lembrete`() = runTest {
        createViewModel().continueFromGoal()
        coVerify { settingsRepository.updateOnboardingStep(OnboardingEligibility.STEP_REMINDER) }
    }

    @Test
    fun `continuar do lembrete agenda 18h e avanca`() = runTest {
        createViewModel().continueFromReminder()
        verify { notificationScheduler.scheduleDailyReminder() }
        coVerify { settingsRepository.updateOnboardingStep(OnboardingEligibility.STEP_LOCATION) }
    }

    @Test
    fun `concluir persiste onboardingCompleted`() = runTest {
        createViewModel().complete()
        coVerify { settingsRepository.completeOnboarding() }
    }

    @Test
    fun `selecionar percentual reutiliza a politica existente`() = runTest {
        settingsFlow.value = AppSettings(presencePolicy = PresencePolicy(freePercentage = 40))
        val viewModel = createViewModel()
        viewModel.uiState.test { awaitLoaded() }
        viewModel.selectPercentage(60)
        coVerify {
            settingsRepository.updatePresencePolicy(
                match { it.freePercentage == 60 && !it.freePercentageEnabled }
            )
        }
        coVerify { widgetRefresher.refresh() }
    }
}
