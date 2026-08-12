package com.presencial.app.domain.location

data class GeoCoordinates(
    val latitude: Double,
    val longitude: Double,
    val stateCode: String? = null,
    val cityName: String? = null
)

interface GeocodingHelper {
    suspend fun geocodeAddress(address: String): Result<GeoCoordinates>
    suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<GeoCoordinates>
}
