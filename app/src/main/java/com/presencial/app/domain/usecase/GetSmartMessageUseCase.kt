package com.presencial.app.domain.usecase

import com.presencial.app.domain.util.SmartMessageEngine
import javax.inject.Inject

class GetSmartMessageUseCase @Inject constructor(
    private val smartMessageEngine: SmartMessageEngine
) {
    operator fun invoke(params: SmartMessageParams): String =
        smartMessageEngine.generate(params)
}
