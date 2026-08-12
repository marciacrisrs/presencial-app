package com.presencial.app.data.remote

import com.presencial.app.domain.usecase.SmartMessageParams
import com.presencial.app.domain.util.LocalSmartMessageEngine
import com.presencial.app.domain.util.SmartMessageVariationSelector
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Integração com IA para mensagens contextuais na seção Inteligência.
 * Usa OpenAI quando há chave configurada; caso contrário, motor local determinístico.
 */
@Singleton
class AiIntelligenceService @Inject constructor(
    private val openAiChatClient: OpenAiChatClient,
    private val promptBuilder: SmartMessagePromptBuilder
) {

    suspend fun fetchSmartMessage(
        params: SmartMessageParams,
        apiKey: String?
    ): String? {
        if (apiKey.isNullOrBlank()) {
            return LocalSmartMessageEngine.generate(params)
        }
        return fetchFromOpenAi(params, apiKey)
    }

    private suspend fun fetchFromOpenAi(
        params: SmartMessageParams,
        apiKey: String
    ): String? {
        val response = openAiChatClient.chatCompletion(
            apiKey = apiKey,
            systemPrompt = promptBuilder.systemPrompt(),
            userPrompt = promptBuilder.buildUserPrompt(params)
        ).getOrNull() ?: return null

        return SmartMessageVariationSelector.selectBest(response)
            ?: SmartMessageVariationSelector.parseVariations(response).firstOrNull()
    }
}
