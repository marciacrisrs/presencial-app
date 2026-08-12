package com.presencial.app.presentation.location.components

internal object LocationUrlParser {
    private const val LOCATION_URL_PREFIX = "presencial://location/"

    fun parse(url: String): Pair<Double, Double>? {
        if (!url.startsWith(LOCATION_URL_PREFIX)) return null
        val parts = url.removePrefix(LOCATION_URL_PREFIX).split("/")
        if (parts.size != 2) return null
        val lat = parts[0].toDoubleOrNull() ?: return null
        val lng = parts[1].toDoubleOrNull() ?: return null
        return lat to lng
    }

    fun build(lat: Double, lng: Double): String = "$LOCATION_URL_PREFIX$lat/$lng"
}
