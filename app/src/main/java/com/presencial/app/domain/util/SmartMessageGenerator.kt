package com.presencial.app.domain.util

import com.presencial.app.domain.usecase.SmartMessageParams

/**
 * Gera mensagens inteligentes para o dashboard com base no progresso atual.
 */
object SmartMessageGenerator {

    private const val PERCENTAGE_THRESHOLD_HIGH = 80f
    private const val DAYS_PER_WEEK = 5.0
    private const val REMAINING_THRESHOLD_LOW = 3
    private const val MIN_WEEKS = 1.0
    private const val PROGRESS_FACTOR_HALF = 2
    private const val SAFETY_MARGIN_FACTOR = 2
    private const val ZERO_DAYS = 0

    fun generate(params: SmartMessageParams): String {
        return when {
            params.requiredDays <= ZERO_DAYS -> "Configure seu percentual de presença nas configurações."
            params.completedDays >= params.requiredDays || params.remainingDays == ZERO_DAYS -> "Meta concluída 🎉"
            else -> generateConditionalMessage(params)
        }
    }

    private fun generateConditionalMessage(params: SmartMessageParams): String {
        val remainingWorkdays = WorkdayCalculator.countRemainingWorkdays(
            params.today, params.yearMonth, params.countSaturdays
        )

        return when {
            params.achievedPercentage >= PERCENTAGE_THRESHOLD_HIGH ->
                "Você já cumpriu ${params.achievedPercentage.toInt()}% da meta."

            params.remainingDays <= remainingWorkdays && remainingWorkdays > ZERO_DAYS ->
                generateRemainingDaysMessage(params.remainingDays, remainingWorkdays)

            params.remainingDays > remainingWorkdays ->
                "Atenção: faltam ${params.remainingDays} dias e restam apenas $remainingWorkdays dias úteis."

            params.completedDays > params.requiredDays / PROGRESS_FACTOR_HALF -> "Você está adiantado."

            remainingWorkdays > params.remainingDays * SAFETY_MARGIN_FACTOR ->
                "Mesmo não indo esta semana, ainda atingirá a meta."

            else -> "Faltam ${params.remainingDays} ${dayLabel(params.remainingDays)} presenciais para a meta."
        }
    }

    private fun generateRemainingDaysMessage(remainingDays: Int, remainingWorkdays: Int): String {
        val weeks = (remainingWorkdays / DAYS_PER_WEEK).coerceAtLeast(MIN_WEEKS)
        return if (remainingDays <= REMAINING_THRESHOLD_LOW) {
            "Faltam apenas $remainingDays ${dayLabel(remainingDays)}."
        } else {
            "Você precisará ir $remainingDays vezes nas próximas ${weeks.toInt()} semanas."
        }
    }

    private fun dayLabel(count: Int): String = if (count == 1) "dia" else "dias"
}
