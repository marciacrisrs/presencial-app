package com.presencial.app.presentation.location

object LocationProminentDisclosure {
    const val FOREGROUND_BODY =
        "O Presencial acessa e coleta dados de localização do dispositivo para registrar sua presença automaticamente quando você está no local de trabalho cadastrado. Esses dados ficam no aparelho."

    const val BACKGROUND_BODY =
        "O Presencial acessa e coleta dados de localização em segundo plano, mesmo com o app fechado, para registrar a presença quando você permanece no local de trabalho cadastrado."

    fun disclosesLocationDataUse(text: String): Boolean {
        val normalized = text.lowercase()
        val mentionsLocation = normalized.contains("localiza")
        val mentionsAccess = normalized.contains("acessa")
        val mentionsCollection = normalized.contains("coleta")
        val mentionsPurpose = normalized.contains("presença") || normalized.contains("presenca")
        return mentionsLocation && mentionsAccess && mentionsCollection && mentionsPurpose
    }

    fun requestSystemPermission(accepted: Boolean, request: () -> Unit) {
        if (accepted) {
            request()
        }
    }
}
