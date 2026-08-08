package com.presencial.app.data.local.mapper

import com.presencial.app.domain.model.DayStatus
import com.presencial.app.util.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EntityMappersTest {

    @Test
    fun `CheckIn with PRESENCIAL status to domain and back`() {
        val domain = TestDataFactory.createCheckIn(status = DayStatus.PRESENCIAL)
        val entity = domain.toEntity()
        assertEquals("PRESENCIAL", entity.status)
        assertEquals(domain.status.name, entity.status)
    }

    @Test
    fun `CheckIn with HOME_OFFICE status to domain and back`() {
        val domain = TestDataFactory.createCheckIn(status = DayStatus.HOME_OFFICE)
        val entity = domain.toEntity()
        assertEquals("HOME_OFFICE", entity.status)
    }

    @Test
    fun `MonthlySummary with high percentage to domain and back`() {
        val domain = TestDataFactory.createMonthlySummary(achievedPercentage = 99.9f)
        val entity = domain.toEntity()
        assertEquals(99.9f, entity.achievedPercentage)
    }

    @Test
    fun `Absence to domain and back`() {
        val domain = TestDataFactory.createAbsence(
            isFullDay = false,
            hours = 4.5f,
            notes = "Dentista",
            isCounted = true
        )
        val entity = domain.toEntity()
        val domainBack = entity.toDomain()

        assertEquals(domain.id, domainBack.id)
        assertEquals(domain.type, domainBack.type)
        assertEquals(domain.startDate, domainBack.startDate)
        assertEquals(domain.endDate, domainBack.endDate)
        assertEquals(domain.isFullDay, domainBack.isFullDay)
        assertEquals(domain.hours, domainBack.hours)
        assertEquals(domain.notes, domainBack.notes)
        assertEquals(domain.isCounted, domainBack.isCounted)
    }

    @Test
    fun `WorkAddress to domain and back`() {
        val domain = TestDataFactory.createWorkAddress(
            addressText = "Rua tal",
            radius = 150f,
            isActive = false
        )
        val entity = domain.toEntity()
        val domainBack = entity.toDomain()

        assertEquals(domain.id, domainBack.id)
        assertEquals(domain.name, domainBack.name)
        assertEquals(domain.addressText, domainBack.addressText)
        assertEquals(domain.latitude, domainBack.latitude)
        assertEquals(domain.longitude, domainBack.longitude)
        assertEquals(domain.radius, domainBack.radius)
        assertEquals(domain.isActive, domainBack.isActive)
    }
}
