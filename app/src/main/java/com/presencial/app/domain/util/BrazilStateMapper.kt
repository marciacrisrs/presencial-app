package com.presencial.app.domain.util

object BrazilStateMapper {

    private val nameToCode = mapOf(
        "ac" to "AC", "acre" to "AC",
        "al" to "AL", "alagoas" to "AL",
        "ap" to "AP", "amapa" to "AP",
        "am" to "AM", "amazonas" to "AM",
        "ba" to "BA", "bahia" to "BA",
        "ce" to "CE", "ceara" to "CE",
        "df" to "DF", "distrito federal" to "DF",
        "es" to "ES", "espirito santo" to "ES",
        "go" to "GO", "goias" to "GO",
        "ma" to "MA", "maranhao" to "MA",
        "mt" to "MT", "mato grosso" to "MT",
        "ms" to "MS", "mato grosso do sul" to "MS",
        "mg" to "MG", "minas gerais" to "MG",
        "pa" to "PA", "para" to "PA",
        "pb" to "PB", "paraiba" to "PB",
        "pr" to "PR", "parana" to "PR",
        "pe" to "PE", "pernambuco" to "PE",
        "pi" to "PI", "piaui" to "PI",
        "rj" to "RJ", "rio de janeiro" to "RJ",
        "rn" to "RN", "rio grande do norte" to "RN",
        "rs" to "RS", "rio grande do sul" to "RS",
        "ro" to "RO", "rondonia" to "RO",
        "rr" to "RR", "roraima" to "RR",
        "sc" to "SC", "santa catarina" to "SC",
        "sp" to "SP", "sao paulo" to "SP",
        "se" to "SE", "sergipe" to "SE",
        "to" to "TO", "tocantins" to "TO"
    )

    fun fromAdminArea(adminArea: String?): String? {
        if (adminArea.isNullOrBlank()) return null
        val normalized = LocationNormalizer.normalize(adminArea)
        if (normalized.length == 2) {
            return normalized.uppercase()
        }
        return nameToCode[normalized]
    }
}
