package com.presencial.app.data.local.mapper

import com.presencial.app.util.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EntityMappersTest {

    @Test
    fun `CheckIn to domain and back`() {
        val domain = TestDataFactory.createCheckIn()
        val entity = domain.toEntity()
        val domainBack = entity.toDomain()

        assertEquals(domain.date, domainBack.date)
        assertEquals(domain.status, domainBack.status)
        assertEquals(domain.updatedAt, domainBack.updatedAt)
    }

    @Test
    fun `MonthlySummary to domain and back`() {
        val domain = TestDataFactory.createMonthlySummary()
        val entity = domain.toEntity()
        val domainBack = entity.toDomain()

        assertEquals(domain.yearMonth, domainBack.yearMonth)
        assertEquals(domain.workdays, domainBack.workdays)
        assertEquals(domain.requiredDays, domainBack.requiredDays)
        assertEquals(domain.completedDays, domainBack.completedDays)
        assertEquals(domain.homeOfficeDays, domainBack.homeOfficeDays)
        assertEquals(domain.requiredPercentage, domainBack.requiredPercentage)
        assertEquals(domain.achievedPercentage, domainBack.achievedPercentage)
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
