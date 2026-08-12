package com.presencial.app.domain.usecase

import com.presencial.app.domain.location.GeocodingHelper
import com.presencial.app.domain.location.GeoCoordinates
import com.presencial.app.domain.repository.WorkAddressRepository
import com.presencial.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ResolveWorkAddressLocationUseCaseTest {

    private val geocodingHelper = mockk<GeocodingHelper>()
    private val repository = mockk<WorkAddressRepository>()
    private val useCase = ResolveWorkAddressLocationUseCase(geocodingHelper, repository)

    @Test
    fun `backfill skips when all addresses already have location`() = runTest {
        coEvery { repository.getAllAddressesSnapshot() } returns listOf(
            TestDataFactory.createWorkAddress(stateCode = "SP", cityName = "São Paulo")
        )

        useCase.backfillMissingLocations()

        coVerify(exactly = 0) { geocodingHelper.reverseGeocode(any(), any()) }
        coVerify(exactly = 0) { repository.updateAddress(any()) }
    }

    @Test
    fun `backfill updates addresses missing state and city`() = runTest {
        val address = TestDataFactory.createWorkAddress(
            stateCode = null,
            cityName = null
        )
        coEvery { repository.getAllAddressesSnapshot() } returns listOf(address)
        coEvery { geocodingHelper.reverseGeocode(address.latitude, address.longitude) } returns Result.success(
            GeoCoordinates(address.latitude, address.longitude, "SP", "São Paulo")
        )
        coEvery { repository.updateAddress(any()) } returns Unit

        useCase.backfillMissingLocations()

        coVerify {
            repository.updateAddress(
                match { it.stateCode == "SP" && it.cityName == "São Paulo" }
            )
        }
    }
}
