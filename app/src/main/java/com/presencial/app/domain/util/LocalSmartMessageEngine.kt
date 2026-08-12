package com.presencial.app.domain.util

import com.presencial.app.domain.usecase.SmartMessageParams

/**
 * Gera mensagens contextuais localmente quando a API de IA não está disponível.
 */
object LocalSmartMessageEngine {

    fun generate(params: SmartMessageParams): String {
        val remaining = params.remainingDays
        val remainingWorkdays = params.remainingWorkdays

        return when {
            params.requiredDays <= 0 ->
                "Configure seu percentual de presença nas configurações."

            params.completedDays >= params.requiredDays ->
                "🎉 Meta batida! Aproveite o home office sem culpa."

            remaining > remainingWorkdays && remainingWorkdays > 0 ->
                "⚠️ Alerta: você precisa ir todos os dias restantes para atingir a meta."

            params.weeklyRequiredDays > 0 &&
                params.weeklyCompletedDays < params.weeklyRequiredDays ->
                "⚠️ Você precisará comparecer ${params.weeklyRequiredDays} vezes nesta semana."

            remainingWorkdays > remaining * SAFETY_MARGIN &&
                remainingWorkdays > 0 &&
                remaining > 0 ->
                "📅 Você pode fazer home office até sexta sem comprometer sua meta."

            remaining <= CLOSE_TO_GOAL_THRESHOLD && remaining > 0 ->
                "🎯 Quase lá! Apenas mais $remaining presenciais e a meta é sua."

            params.achievedPercentage < LOW_PROGRESS_THRESHOLD ->
                "🚀 Início de mês! Que tal planejar 2 presenciais para esta semana?"

            params.projectedMonthPercentage > 0 ->
                "🎯 Se mantiver o ritmo atual, terminará o mês com ${params.projectedMonthPercentage}%."

            else ->
                "📅 Faltam $remaining ${dayLabel(remaining)} para a meta."
        }
    }

    private fun dayLabel(count: Int): String = if (count == 1) "dia" else "dias"

    private const val SAFETY_MARGIN = 2
    private const val CLOSE_TO_GOAL_THRESHOLD = 3
    private const val LOW_PROGRESS_THRESHOLD = 30f
}
