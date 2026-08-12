package com.presencial.app.data.holidays

import android.content.Context
import com.presencial.app.domain.model.RegionalLocation
import com.presencial.app.domain.util.HolidayCalculator
import com.presencial.app.domain.util.LocationNormalizer
import com.presencial.app.domain.util.RegionalHolidayLookup
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.time.LocalDate
import java.time.Month
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegionalHolidayCatalog @Inject constructor(
    @ApplicationContext context: Context
) : RegionalHolidayLookup {

    private val stateHolidays: Map<String, List<HolidayDefinition>>
    private val cityHolidays: Map<String, List<HolidayDefinition>>

    init {
        val json = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        stateHolidays = parseSection(root.getJSONObject("states"))
        cityHolidays = parseSection(root.getJSONObject("cities"))
    }

    override fun holidaysForLocation(year: Int, location: RegionalLocation): List<HolidayCalculator.Holiday> {
        val stateDefs = stateHolidays[location.stateCode.uppercase()].orEmpty()
        val cityKey = LocationNormalizer.cityKey(location.stateCode, location.cityName)
        val cityDefs = cityHolidays[cityKey].orEmpty()
        return (stateDefs + cityDefs)
            .distinctBy { it.month to it.day }
            .map { it.toHoliday(year) }
    }

    private fun parseSection(section: JSONObject): Map<String, List<HolidayDefinition>> {
        val result = mutableMapOf<String, List<HolidayDefinition>>()
        section.keys().forEach { key ->
            val array = section.getJSONArray(key)
            val holidays = buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        HolidayDefinition(
                            month = item.getInt("month"),
                            day = item.getInt("day"),
                            name = item.getString("name")
                        )
                    )
                }
            }
            result[key] = holidays
        }
        return result
    }

    private data class HolidayDefinition(
        val month: Int,
        val day: Int,
        val name: String
    ) {
        fun toHoliday(year: Int): HolidayCalculator.Holiday =
            HolidayCalculator.Holiday(
                date = LocalDate.of(year, Month.of(month), day),
                name = name
            )
    }

    companion object {
        private const val ASSET_FILE = "regional_holidays.json"
    }
}
