package com.presencial.app.domain.util

object SmartMessageVariationSelector {

    private val numberedLineRegex = Regex("^\\s*\\d+[.)\\-]\\s*(.+)$")
    private val emojiRegex = Regex("[\\p{So}\\p{Sk}\\u2600-\\u27BF\\uD83C-\\uDBFF\\uDC00-\\uDFFF]")

    fun selectBest(rawResponse: String): String? {
        val candidates = parseVariations(rawResponse)
            .filter { it.length in MIN_LENGTH..MAX_LENGTH }
            .distinct()

        return candidates.maxByOrNull(::score)
    }

    fun parseVariations(rawResponse: String): List<String> =
        rawResponse.lines()
            .mapNotNull { line ->
                numberedLineRegex.find(line.trim())?.groupValues?.get(1)?.trim()
                    ?: line.trim().takeIf { it.isNotEmpty() && !it.startsWith("{") }
            }
            .filter { it.isNotEmpty() }

    private fun score(message: String): Int {
        var points = 0
        if (emojiRegex.containsMatchIn(message.take(4))) points += PRIORITY_EMOJI
        if (message.contains("meta", ignoreCase = true)) points += PRIORITY_KEYWORD
        if (message.contains("semana", ignoreCase = true)) points += PRIORITY_KEYWORD
        if (message.contains("ritmo", ignoreCase = true)) points += PRIORITY_KEYWORD
        points += (MAX_LENGTH - kotlin.math.abs(message.length - TARGET_LENGTH)).coerceAtLeast(0)
        return points
    }

    private const val MIN_LENGTH = 30
    private const val MAX_LENGTH = 90
    private const val TARGET_LENGTH = 60
    private const val PRIORITY_EMOJI = 20
    private const val PRIORITY_KEYWORD = 5
}
