package com.presencial.app.domain.util

import java.text.Normalizer

object LocationNormalizer {

    fun normalize(value: String): String =
        Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase()

    fun cityKey(stateCode: String, cityName: String): String =
        "${stateCode.uppercase()}|${normalize(cityName)}"
}
