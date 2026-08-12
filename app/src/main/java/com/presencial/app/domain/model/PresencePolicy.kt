package com.presencial.app.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

enum class WeekParity {
    EVEN,
    ODD
}

enum class PolicyConflictPriority {
    /** Usa o maior valor entre percentual livre e dias das outras regras. */
    UNION_MAX,
    /** Dias fixos e semanas alternadas somam; percentual só eleva se for maior. */
    FIXED_FIRST
}

/**
 * Política de presença configurável (issue #5).
 * Combina percentual livre, dias fixos obrigatórios e semanas alternadas.
 */
data class PresencePolicy(
    val companyName: String = "",
    val freePercentageEnabled: Boolean = true,
    val freePercentage: Int = 40,
    val fixedWeekdaysEnabled: Boolean = false,
    val mandatoryWeekdays: Set<DayOfWeek> = emptySet(),
    val alternatingWeeksEnabled: Boolean = false,
    val alternatingAnchorDate: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY),
    val onSiteWeekParity: WeekParity = WeekParity.EVEN,
    val conflictPriority: PolicyConflictPriority = PolicyConflictPriority.UNION_MAX
) {
    fun normalized(): PresencePolicy = copy(
        companyName = companyName.trim(),
        freePercentage = freePercentage.coerceIn(MIN_PERCENTAGE, MAX_PERCENTAGE),
        mandatoryWeekdays = mandatoryWeekdays.sortedBy { it.value }.toSet()
    )

    companion object {
        const val MIN_PERCENTAGE = 1
        const val MAX_PERCENTAGE = 100

        fun fromLegacyPercentage(percentage: Int): PresencePolicy =
            PresencePolicy(freePercentage = percentage.coerceIn(MIN_PERCENTAGE, MAX_PERCENTAGE))
    }
}

data class PolicyValidationResult(
    val isValid: Boolean,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList()
)

data class WeeklyPolicySummary(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val isOnSiteWeek: Boolean,
    val requiredDates: List<LocalDate>,
    val requiredCount: Int
)
