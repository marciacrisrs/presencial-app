package com.presencial.app.data.local.migration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DatabaseMigrationsTest {

    @Test
    fun `migrations cobrem cada versao ate o schema atual`() {
        val ordered = ALL_MIGRATIONS.sortedBy { it.startVersion }
        assertEquals(1, ordered.first().startVersion)
        assertEquals(5, ordered.last().endVersion)
        ordered.zipWithNext().forEach { (current, next) ->
            assertEquals(current.endVersion, next.startVersion)
        }
    }

    @Test
    fun `nao ha buraco entre versoes`() {
        val versions = ALL_MIGRATIONS.flatMap { listOf(it.startVersion, it.endVersion) }.toSet()
        assertTrue((1..5).all { it in versions })
    }
}
