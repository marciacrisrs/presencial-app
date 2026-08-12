package com.presencial.app.domain.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SmartMessageVariationSelectorTest {

    @Test
    fun `selectBest should pick message with emoji and valid length`() {
        val raw = """
            1. ok curta
            2. 📅 Você pode fazer home office até sexta sem comprometer sua meta.
            3. 🎯 Se mantiver o ritmo atual, terminará o mês com 67% da meta hoje.
        """.trimIndent()

        val selected = SmartMessageVariationSelector.selectBest(raw)

        assertNotNull(selected)
        assertTrue(selected!!.length in 30..90)
        assertTrue(selected.contains("67") || selected.contains("semana") || selected.contains("home office"))
    }

    @Test
    fun `parseVariations should extract numbered lines`() {
        val raw = """
            1. Primeira mensagem com emoji 📅 e tamanho suficiente para passar.
            2. Segunda mensagem com emoji ⚠️ e tamanho suficiente para passar.
        """.trimIndent()

        val variations = SmartMessageVariationSelector.parseVariations(raw)

        assertEquals(2, variations.size)
        assertTrue(variations.first().startsWith("Primeira"))
    }
}
