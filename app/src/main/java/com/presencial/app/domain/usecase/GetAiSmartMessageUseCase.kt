package com.presencial.app.domain.usecase

import com.presencial.app.data.remote.AiIntelligenceService
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.util.SmartMessageFallback
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetAiSmartMessageUseCase @Inject constructor(
    private val aiService: AiIntelligenceService,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(params: SmartMessageParams): String {
        val apiKey = settingsRepository.settings.first().openAiApiKey.trim()

        val aiMessage = runCatching {
            aiService.fetchSmartMessage(params, apiKey.takeIf { it.isNotEmpty() })
        }.getOrNull()

        return aiMessage?.takeIf { it.isNotBlank() }
            ?: SmartMessageFallback.generate(params)
    }
}
