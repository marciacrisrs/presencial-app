package com.presencial.app.data.remote

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

/**
 * Serviço para integração com a camada de Inteligência Artificial.
 */
@Singleton
class AiIntelligenceService @Inject constructor() {

    /**
     * Simula a chamada para um backend que consulta o LLM (ChatGPT/Gemini).
     */
    suspend fun fetchSmartMessage(
        completed: Int,
        required: Int,
        remainingWorkdays: Int,
        percentage: Int
    ): String? {
        // Simula latência de rede/processamento da IA
        delay(SIMULATED_DELAY.milliseconds)

        // Simula falha ocasional para testar o fallback (5% de chance)
        if (Random.nextInt(PROBABILITY_BASE) < FAIL_CHANCE) return null

        val remaining = (required - completed).coerceAtLeast(0)

        return when {
            completed >= required -> "🎉 Meta batida! Aproveite o home office sem culpa."

            remaining > remainingWorkdays -> "⚠️ Alerta: Você precisa ir todos os dias restantes para atingir a meta."

            remainingWorkdays > remaining * 2 -> "📅 Você pode fazer home office até sexta sem comprometer sua meta."

            remaining <= REMAINING_THRESHOLD && remaining > 0 -> 
                "🎯 Quase lá! Apenas mais $remaining presenciais e a meta é sua."

            percentage < PERCENTAGE_LOW -> "🚀 Início de mês! Que tal planejar 2 presenciais para esta semana?"

            else -> "🎯 Se mantiver o ritmo atual, terminará o mês com ${percentage + BONUS_PERCENT}% da meta."
        }
    }

    companion object {
        private const val SIMULATED_DELAY = 1500L
        private const val PROBABILITY_BASE = 100
        private const val FAIL_CHANCE = 5
        private const val REMAINING_THRESHOLD = 3
        private const val PERCENTAGE_LOW = 30
        private const val BONUS_PERCENT = 5
    }
}
