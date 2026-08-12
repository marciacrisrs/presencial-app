package com.presencial.app.domain.usecase

import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.repository.AbsenceRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.util.FakeTimeProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate

class GetWeeklyPolicySummaryUseCaseTest {

    private val absenceRepository = mockk<AbsenceRepository>()
    private val settingsRepository = mockk<SettingsRepository>()
    private val timeProvider = FakeTimeProvider()
    private lateinit var useCase: GetWeeklyPolicySummaryUseCase

    @BeforeEach
    fun setup() {
        timeProvider.setToday(LocalDate.of(2026, 8, 12))
        useCase = GetWeeklyPolicySummaryUseCase(absenceRepository, settingsRepository, timeProvider)
    }

    @Test
    fun `should return weekly summaries for fixed weekdays policy`() = runTest {
        val policy = PresencePolicy(
            freePercentageEnabled = false,
            fixedWeekdaysEnabled = true,
            mandatoryWeekdays = setOf(DayOfWeek.MONDAY)
        )
        every { absenceRepository.getAbsencesInRange(any(), any()) } returns flowOf(emptyList())
        every { settingsRepository.settings } returns flowOf(
            AppSettings(presencePolicy = policy)
        )

        val summaries = useCase().first()

        assertTrue(summaries.isNotEmpty())
        assertTrue(summaries.any { it.requiredCount > 0 })
    }
}
