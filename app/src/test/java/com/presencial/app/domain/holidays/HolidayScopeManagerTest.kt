package com.presencial.app.domain.holidays

import com.presencial.app.domain.model.RegionalLocation
import com.presencial.app.domain.repository.WorkAddressRepository
import com.presencial.app.domain.util.HolidayCalculator
import com.presencial.app.domain.util.RegionalHolidayLookup
import com.presencial.app.util.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class HolidayScopeManagerTest {

    private val repository = mockk<WorkAddressRepository>()

    @AfterEach
    fun tearDown() {
        HolidayCalculator.clearRegionalHolidays()
    }

    @Test
    fun `configures regional holidays from work addresses with location metadata`() = runTest {
        val lookup = RegionalHolidayLookup { year, location ->
            if (location.stateCode == "SP" && location.cityName == "São Paulo") {
                listOf(
                    HolidayCalculator.Holiday(
                        LocalDate.of(year, 1, 25),
                        "Aniversário de São Paulo"
                    )
                )
            } else {
                emptyList()
            }
        }
        every { repository.getAllAddresses() } returns MutableStateFlow(
            listOf(TestDataFactory.createWorkAddress())
        )

        HolidayScopeManager(repository, lookup, UnconfinedTestDispatcher())

        assertEquals(
            setOf(RegionalLocation("SP", "São Paulo")),
            HolidayCalculator.currentRegionalLocations()
        )
        assertTrue(HolidayCalculator.isHoliday(LocalDate.of(2026, 1, 25)))
    }

    @Test
    fun `clears regional holidays when no work addresses remain`() = runTest {
        val lookup = mockk<RegionalHolidayLookup>(relaxed = true)
        val addresses = MutableStateFlow(listOf(TestDataFactory.createWorkAddress()))
        every { repository.getAllAddresses() } returns addresses

        HolidayScopeManager(repository, lookup, UnconfinedTestDispatcher())
        assertEquals(1, HolidayCalculator.currentRegionalLocations().size)

        addresses.value = emptyList()

        assertTrue(HolidayCalculator.currentRegionalLocations().isEmpty())
    }

    @Test
    fun `ignores addresses without coordinates or regional metadata`() = runTest {
        val lookup = mockk<RegionalHolidayLookup>(relaxed = true)
        every { repository.getAllAddresses() } returns MutableStateFlow(
            listOf(
                TestDataFactory.createWorkAddress(
                    latitude = 0.0,
                    longitude = 0.0,
                    stateCode = "SP",
                    cityName = "São Paulo"
                ),
                TestDataFactory.createWorkAddress(
                    stateCode = null,
                    cityName = null
                )
            )
        )

        HolidayScopeManager(repository, lookup, UnconfinedTestDispatcher())

        assertTrue(HolidayCalculator.currentRegionalLocations().isEmpty())
    }
}
