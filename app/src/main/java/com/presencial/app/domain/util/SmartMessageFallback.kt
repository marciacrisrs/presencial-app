package com.presencial.app.domain.util

import com.presencial.app.domain.usecase.SmartMessageParams

object SmartMessageFallback {

    fun generate(params: SmartMessageParams): String = when {
        params.requiredDays <= 0 ->
            "Configure seu percentual de presença nas configurações."
        params.completedDays >= params.requiredDays || params.remainingDays == 0 ->
            "Meta concluída 🎉"
        else -> {
            val days = params.remainingDays
            "Faltam $days ${if (days == 1) "dia" else "dias"}."
        }
    }
}
