package com.presencial.app.domain.util

data class PresenceProgressCopy(
    val daysLine: String,
    val remainingLine: String,
    val policyLine: String,
    val isGoalMet: Boolean
)

object PresenceProgressPresentation {
    fun from(
        completedDays: Int,
        requiredDays: Int,
        remainingDays: Int,
        policyPercentage: Int,
        companyName: String = ""
    ): PresenceProgressCopy {
        val remaining = remainingDays.coerceAtLeast(0)
        val required = requiredDays.coerceAtLeast(0)
        val completed = completedDays.coerceAtLeast(0)
        val isGoalMet = required > 0 && remaining <= 0
        return PresenceProgressCopy(
            daysLine = "$completed de $required dias presenciais",
            remainingLine = remainingLine(required, remaining),
            policyLine = policyLine(policyPercentage, companyName),
            isGoalMet = isGoalMet
        )
    }

    private fun remainingLine(required: Int, remaining: Int): String = when {
        required <= 0 -> "Configure sua meta"
        remaining <= 0 -> "Meta atingida"
        remaining == 1 -> "Falta 1 dia"
        else -> "Faltam $remaining dias"
    }

    private fun policyLine(policyPercentage: Int, companyName: String): String {
        val company = companyName.trim()
        val rule = "Regra: $policyPercentage% dos dias úteis"
        return if (company.isEmpty()) rule else "$rule · $company"
    }
}
