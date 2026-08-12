package com.presencial.app.data.location

import android.content.Context
import android.location.Geocoder
import com.presencial.app.di.IoDispatcher
import com.presencial.app.domain.location.GeoCoordinates
import com.presencial.app.domain.location.GeocodingHelper
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
                if (!Geocoder.isPresent()) {
                    error("Geocoding indisponível neste dispositivo")
                }
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocationName(address, 1)
                    ?: error("Nenhum resultado encontrado para o endereço")
                val location = results.firstOrNull()
                    ?: error("Nenhum resultado encontrado para o endereço")
                GeoCoordinates(location.latitude, location.longitude)
            }
        }
}
