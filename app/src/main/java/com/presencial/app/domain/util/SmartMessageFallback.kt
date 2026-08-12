package com.presencial.app.domain.util

import com.presencial.app.domain.usecase.SmartMessageParams
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartMessageFallback @Inject constructor(
    private val texts: SmartMessageTextProvider
) {

    fun generate(params: SmartMessageParams): String = when {
        params.requiredDays <= 0 ->
            texts.configureRequiredPercentage()
        params.completedDays >= params.requiredDays || params.remainingDays == 0 ->
            texts.fallbackGoalCompleted()
        else -> texts.fallbackRemainingDays(params.remainingDays)
    }
}
