package com.presencial.app.domain.usecase

import com.presencial.app.domain.location.GeocodingHelper
import com.presencial.app.domain.model.WorkAddress
import com.presencial.app.domain.repository.WorkAddressRepository
import javax.inject.Inject

class ResolveWorkAddressLocationUseCase @Inject constructor(
    private val geocodingHelper: GeocodingHelper,
    private val workAddressRepository: WorkAddressRepository
) {

    suspend fun resolve(latitude: Double, longitude: Double): Pair<String?, String?> {
        if (latitude == 0.0 && longitude == 0.0) return null to null
        return geocodingHelper.reverseGeocode(latitude, longitude)
            .getOrNull()
            ?.let { it.stateCode to it.cityName }
            ?: (null to null)
    }

    suspend fun backfillMissingLocations() {
        val pending = workAddressRepository.getAllAddressesSnapshot()
            .filter { it.needsLocationBackfill() }
        if (pending.isEmpty()) return

        pending.forEach { address ->
            val (stateCode, cityName) = resolve(address.latitude, address.longitude)
            if (stateCode != null && cityName != null) {
                workAddressRepository.updateAddress(
                    address.copy(stateCode = stateCode, cityName = cityName)
                )
            }
        }
    }

    private fun WorkAddress.needsLocationBackfill(): Boolean =
        (latitude != 0.0 || longitude != 0.0) &&
            (stateCode.isNullOrBlank() || cityName.isNullOrBlank())
}
