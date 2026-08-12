package com.presencial.app.domain.location

data class GeoCoordinates(
    val latitude: Double,
    val longitude: Double
)

interface GeocodingHelper {
    suspend fun geocodeAddress(address: String): Result<GeoCoordinates>
}
