package com.presencial.app.domain.location

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class GeofenceRequestParserTest {

    @Test
    fun `parseWorkAddressId should return long for numeric request id`() {
        assertEquals(42L, GeofenceRequestParser.parseWorkAddressId("42"))
    }

    @Test
    fun `parseWorkAddressId should return null for invalid request id`() {
        assertNull(GeofenceRequestParser.parseWorkAddressId("not-a-number"))
        assertNull(GeofenceRequestParser.parseWorkAddressId(null))
    }
}
