package com.presencial.app.data.location

import android.content.Context
import android.location.Geocoder
import com.presencial.app.di.IoDispatcher
import com.presencial.app.domain.location.GeoCoordinates
import com.presencial.app.domain.location.GeocodingHelper
import com.presencial.app.domain.util.BrazilStateMapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidGeocodingHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : GeocodingHelper {

    override suspend fun geocodeAddress(address: String): Result<GeoCoordinates> =
        withContext(ioDispatcher) {
            runCatching {
                if (address.isBlank()) error("Endereço vazio")
                if (!Geocoder.isPresent()) {
                    error("Geocoding indisponível neste dispositivo")
                }
                val geocoder = Geocoder(context, LOCALE)
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocationName(address, 1)
                    ?: error("Nenhum resultado encontrado para o endereço")
                val location = results.firstOrNull()
                    ?: error("Nenhum resultado encontrado para o endereço")
                toGeoCoordinates(location.latitude, location.longitude, location)
            }
        }

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<GeoCoordinates> =
        withContext(ioDispatcher) {
            runCatching {
                if (!Geocoder.isPresent()) {
                    error("Geocoding indisponível neste dispositivo")
                }
                val geocoder = Geocoder(context, LOCALE)
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocation(latitude, longitude, 1)
                    ?: error("Nenhum resultado encontrado para as coordenadas")
                val location = results.firstOrNull()
                    ?: error("Nenhum resultado encontrado para as coordenadas")
                toGeoCoordinates(latitude, longitude, location)
            }
        }

    private fun toGeoCoordinates(
        latitude: Double,
        longitude: Double,
        location: android.location.Address
    ): GeoCoordinates {
        val stateCode = BrazilStateMapper.fromAdminArea(location.adminArea)
        val cityName = location.locality?.takeIf { it.isNotBlank() }
            ?: location.subAdminArea?.takeIf { it.isNotBlank() }
        return GeoCoordinates(
            latitude = latitude,
            longitude = longitude,
            stateCode = stateCode,
            cityName = cityName
        )
    }

    companion object {
        private val LOCALE = Locale("pt", "BR")
    }
}
