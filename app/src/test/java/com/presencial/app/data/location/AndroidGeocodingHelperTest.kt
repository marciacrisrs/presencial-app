package com.presencial.app.data.location

import android.content.Context
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AndroidGeocodingHelperTest {

    @Test
    fun `when address is blank, then return failure`() = runTest {
        val context = mockk<Context>(relaxed = true)
        val helper = AndroidGeocodingHelper(context, Dispatchers.IO)

        val result = helper.geocodeAddress("   ")

        assertTrue(result.isFailure)
    }
}
