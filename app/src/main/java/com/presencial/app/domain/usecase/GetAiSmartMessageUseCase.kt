package com.presencial.app.domain.usecase

import com.presencial.app.data.remote.AiIntelligenceService
import com.presencial.app.domain.util.SmartMessageGenerator
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class GetAiSmartMessageUseCase @Inject constructor(
    private val aiService: AiIntelligenceService
) {
    suspend operator fun invoke(
        completedDays: Int,
        requiredDays: Int,
        remainingDays: Int,
        achievedPercentage: Float,
        today: LocalDate,
        yearMonth: YearMonth,
        countSaturdays: Boolean
    ): String {
        return try {
            // Tenta obter a mensagem da IA
            val aiMessage = aiService.fetchSmartMessage(
                completed = completedDays,
                required = requiredDays,
                remainingWorkdays = remainingDays, // No dashboard chamamos WorkdayCalculator externamente
                percentage = achievedPercentage.toInt()
            )

            // Fallback se a IA retornar vazio ou falhar
            aiMessage ?: SmartMessageGenerator.generate(
                completedDays, requiredDays, remainingDays, achievedPercentage,
                today, yearMonth, countSaturdays
            )
        } catch (e: Exception) {
            // Fallback em caso de erro de rede/serviço
            SmartMessageGenerator.generate(
                completedDays, requiredDays, remainingDays, achievedPercentage,
                today, yearMonth, countSaturdays
            )
        }
    }
}
