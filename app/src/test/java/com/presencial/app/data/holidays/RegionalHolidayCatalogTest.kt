package com.presencial.app.data.holidays

import android.content.Context
import android.content.res.AssetManager
import com.presencial.app.domain.model.RegionalLocation
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class RegionalHolidayCatalogTest {

    private val catalog: RegionalHolidayCatalog by lazy {
        val assets = mockk<AssetManager>()
        every { assets.open("regional_holidays.json") } returns
            requireNotNull(javaClass.getResourceAsStream("/regional_holidays_fixture.json"))
        val context = mockk<Context>()
        every { context.assets } returns assets
        RegionalHolidayCatalog(context)
    }

    @Test
    fun `returns state holidays for location`() {
        val holidays = catalog.holidaysForLocation(
            year = 2026,
            location = RegionalLocation("SP", "Campinas")
        )

        assertEquals(1, holidays.size)
        assertEquals(LocalDate.of(2026, 7, 9), holidays[0].date)
        assertEquals("Revolução Constitucionalista (SP)", holidays[0].name)
    }

    @Test
    fun `merges state and city holidays without duplicates`() {
        val holidays = catalog.holidaysForLocation(
            year = 2026,
            location = RegionalLocation("SP", "São Paulo")
        )

        assertEquals(2, holidays.size)
        assertTrue(holidays.any { it.date == LocalDate.of(2026, 1, 25) })
        assertTrue(holidays.any { it.date == LocalDate.of(2026, 7, 9) })
    }

    @Test
    fun `returns empty list for unknown state`() {
        val holidays = catalog.holidaysForLocation(
            year = 2026,
            location = RegionalLocation("XX", "Cidade")
        )

        assertTrue(holidays.isEmpty())
    }

    @Test
    fun `matches city keys case insensitively via normalizer`() {
        val holidays = catalog.holidaysForLocation(
            year = 2026,
            location = RegionalLocation("rj", "Rio de Janeiro")
        )

        assertEquals(2, holidays.size)
        assertTrue(holidays.any { it.name == "São Jorge (RJ)" })
        assertTrue(holidays.any { it.name == "São Sebastião (Rio de Janeiro)" })
    }
}
