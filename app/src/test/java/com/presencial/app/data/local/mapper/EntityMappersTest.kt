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
        assertEquals(domain.achievedPercentage, domainBack.achievedPercentage)
    }

    @Test
    fun `Absence to domain and back`() {
        val domain = TestDataFactory.createAbsence()
        val entity = domain.toEntity()
        val domainBack = entity.toDomain()

        assertEquals(domain.id, domainBack.id)
        assertEquals(domain.type, domainBack.type)
        assertEquals(domain.startDate, domainBack.startDate)
    }

    @Test
    fun `WorkAddress to domain and back`() {
        val domain = TestDataFactory.createWorkAddress()
        val entity = domain.toEntity()
        val domainBack = entity.toDomain()

        assertEquals(domain.id, domainBack.id)
        assertEquals(domain.name, domainBack.name)
        assertEquals(domain.latitude, domainBack.latitude)
    }
}
