package com.presencial.app.presentation.location

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class LocationProminentDisclosureTest {

    @Test
    fun `declaracao em destaque divulga acesso e coleta de localizacao`() {
        assertTrue(LocationProminentDisclosure.disclosesLocationDataUse(LocationProminentDisclosure.FOREGROUND_BODY))
        assertTrue(LocationProminentDisclosure.disclosesLocationDataUse(LocationProminentDisclosure.BACKGROUND_BODY))
    }

    @Test
    fun `textos do app usam a declaracao de acesso e coleta`() {
        val xml = File("src/main/res/values/strings.xml").readText()
        assertTrue(xml.contains(LocationProminentDisclosure.FOREGROUND_BODY))
        assertTrue(xml.contains(LocationProminentDisclosure.BACKGROUND_BODY))
    }

    @Test
    fun `politica de privacidade divulga coleta de localizacao inclusive em segundo plano`() {
        val inApp = File("src/main/res/values/strings_privacy.xml").readText()
        val hosted = File("../docs/privacy.html").readText()
        listOf(inApp, hosted).forEach { policy ->
            assertTrue(LocationProminentDisclosure.disclosesLocationDataUse(policy))
            assertTrue(policy.contains("segundo plano"))
        }
        assertTrue(hosted.contains("background location"))
    }

    @Test
    fun `permissao do sistema so abre depois do consentimento`() {
        var requested = false
        LocationProminentDisclosure.requestSystemPermission(accepted = false) { requested = true }
        assertFalse(requested)

        LocationProminentDisclosure.requestSystemPermission(accepted = true) { requested = true }
        assertTrue(requested)
    }
}
