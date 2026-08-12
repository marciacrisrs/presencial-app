package com.presencial.app.presentation.location.components

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class LocationUrlParserTest {

    @Test
    fun `parse should return coordinates from custom scheme url`() {
        val result = LocationUrlParser.parse("presencial://location/-23.55/-46.63")

        assertEquals(-23.55 to -46.63, result)
    }

    @Test
    fun `parse should reject invalid urls`() {
        assertNull(LocationUrlParser.parse("https://example.com"))
        assertNull(LocationUrlParser.parse("presencial://location/abc/def"))
    }
}
