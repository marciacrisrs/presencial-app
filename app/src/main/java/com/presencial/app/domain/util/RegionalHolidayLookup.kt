package com.presencial.app.domain.util

import com.presencial.app.domain.model.RegionalLocation

fun interface RegionalHolidayLookup {
    fun holidaysForLocation(year: Int, location: RegionalLocation): List<HolidayCalculator.Holiday>
}
