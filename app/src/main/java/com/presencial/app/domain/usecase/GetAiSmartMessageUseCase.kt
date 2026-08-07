package com.presencial.app.domain.usecase

import com.presencial.app.data.remote.AiIntelligenceService
import com.presencial.app.domain.util.SmartMessageGenerator
import javax.inject.Inject

class GetAiSmartMessageUseCase @Inject constructor(
    private val aiService: AiIntelligenceService
) {
    suspend operator fun invoke(params: SmartMessageParams): String {
        val aiResult = runCatching {
            aiService.fetchSmartMessage(
                completed = params.completedDays,
                required = params.requiredDays,
                remainingWorkdays = params.remainingDays,
                percentage = params.achievedPercentage.toInt()
            )
        }

        return aiResult.getOrNull() ?: SmartMessageGenerator.generate(params)
    }
}
