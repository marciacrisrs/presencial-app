package com.presencial.app.domain.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class BrazilStateMapperTest {

    @Test
    fun `maps state abbreviations and full names`() {
        assertEquals("SP", BrazilStateMapper.fromAdminArea("SP"))
        assertEquals("SP", BrazilStateMapper.fromAdminArea("São Paulo"))
        assertEquals("RJ", BrazilStateMapper.fromAdminArea("Rio de Janeiro"))
        assertEquals("MG", BrazilStateMapper.fromAdminArea("Minas Gerais"))
    }

    @Test
    fun `returns null for blank admin area`() {
        assertNull(BrazilStateMapper.fromAdminArea(null))
        assertNull(BrazilStateMapper.fromAdminArea(""))
    }
}
