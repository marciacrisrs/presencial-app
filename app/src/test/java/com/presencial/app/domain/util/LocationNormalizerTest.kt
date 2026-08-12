package com.presencial.app.domain.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LocationNormalizerTest {

    @Test
    fun `normalize removes accents and lowercases`() {
        assertEquals("sao paulo", LocationNormalizer.normalize("São Paulo"))
        assertEquals("rio de janeiro", LocationNormalizer.normalize("  Rio de Janeiro  "))
    }

    @Test
    fun `cityKey combines state and normalized city`() {
        assertEquals("SP|sao paulo", LocationNormalizer.cityKey("sp", "São Paulo"))
        assertEquals("RJ|niteroi", LocationNormalizer.cityKey("RJ", "Niterói"))
    }
}
