package com.presencial.app.data.remote

import com.presencial.app.domain.usecase.SmartMessageParams
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartMessagePromptBuilder @Inject constructor() {

    fun buildUserPrompt(params: SmartMessageParams): String = buildString {
        appendLine("Gere exatamente 3 mensagens curtas em português do Brasil (30 a 90 caracteres cada).")
        appendLine("Use um emoji no início de cada mensagem (📅, ⚠️, 🎯, 🚀 ou 🎉).")
        appendLine("Seja informativo e acionável. Não dê conselhos legais ou contratuais.")
        appendLine("Use apenas as métricas abaixo — não invente dados.")
        appendLine()
        appendLine("Métricas:")
        appendLine("- Presenças no mês: ${params.completedDays} de ${params.requiredDays}")
        appendLine("- Faltam: ${params.remainingDays} presenciais")
        appendLine("- Dias úteis restantes no mês: ${params.remainingWorkdays}")
        appendLine("- Progresso atual: ${params.achievedPercentage.toInt()}%")
        appendLine("- Projeção mantendo o ritmo: ${params.projectedMonthPercentage}%")
        appendLine("- Presenças nesta semana: ${params.weeklyCompletedDays}")
        appendLine("- Necessidade estimada nesta semana: ${params.weeklyRequiredDays}")
        appendLine()
        appendLine("Responda SOMENTE com 3 linhas numeradas (1. 2. 3.), sem explicações.")
    }

    fun systemPrompt(): String =
        "Você é o assistente de inteligência do app Presencial, que ajuda usuários " +
            "a acompanhar metas de presença presencial no trabalho."
}
