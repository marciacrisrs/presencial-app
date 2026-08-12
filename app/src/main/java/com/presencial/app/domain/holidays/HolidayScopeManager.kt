package com.presencial.app.domain.holidays

import com.presencial.app.di.IoDispatcher
import com.presencial.app.domain.model.RegionalLocation
import com.presencial.app.domain.model.WorkAddress
import com.presencial.app.domain.repository.WorkAddressRepository
import com.presencial.app.domain.util.HolidayCalculator
import com.presencial.app.domain.util.RegionalHolidayLookup
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HolidayScopeManager @Inject constructor(
    private val workAddressRepository: WorkAddressRepository,
    private val regionalHolidayLookup: RegionalHolidayLookup,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    init {
        scope.launch {
            workAddressRepository.getAllAddresses()
                .map { addresses -> addresses.toRegionalLocations() }
                .distinctUntilChanged()
                .collect { locations ->
                    if (locations.isEmpty()) {
                        HolidayCalculator.clearRegionalHolidays()
                    } else {
                        HolidayCalculator.configureRegionalHolidays(
                            lookup = regionalHolidayLookup,
                            locations = locations
                        )
                    }
                }
        }
    }

    private fun List<WorkAddress>.toRegionalLocations(): Set<RegionalLocation> =
        mapNotNull { address ->
            if (!address.hasCoordinates()) return@mapNotNull null
            val stateCode = address.stateCode ?: return@mapNotNull null
            val cityName = address.cityName ?: return@mapNotNull null
            RegionalLocation(stateCode = stateCode, cityName = cityName)
        }.toSet()

    private fun WorkAddress.hasCoordinates(): Boolean =
        latitude != 0.0 || longitude != 0.0
}
