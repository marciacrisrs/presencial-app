package com.presencial.app.data.repository

import app.cash.turbine.test
import com.presencial.app.data.local.dao.AbsenceDao
import com.presencial.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AbsenceRepositoryImplTest {

    private val absenceDao: AbsenceDao = mockk()
    private lateinit var repository: AbsenceRepositoryImpl

    @BeforeEach
    fun setup() {
        repository = AbsenceRepositoryImpl(absenceDao)
    }

    @Test
    fun `when getAllAbsences, then return domain list from dao`() = runTest {
        // Arrange
        val entities = listOf(TestDataFactory.createAbsenceEntity())
        every { absenceDao.getAllAbsences() } returns flowOf(entities)

        // Act & Assert
        repository.getAllAbsences().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(entities[0].id, result[0].id)
            awaitComplete()
        }
    }

    @Test
    fun `when getAbsencesInRange, then return domain list from dao`() = runTest {
        // Arrange
        val start = LocalDate.of(2026, 8, 1)
        val end = LocalDate.of(2026, 8, 31)
        val entities = listOf(TestDataFactory.createAbsenceEntity(id = 1))
        every { absenceDao.getAbsencesInRange(start.toEpochDay(), end.toEpochDay()) } returns flowOf(entities)

        // Act & Assert
        repository.getAbsencesInRange(start, end).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(entities[0].id, result[0].id)
            awaitComplete()
        }
    }

    @Test
    fun `when insertAbsence, then dao insertAbsence is called`() = runTest {
        // Arrange
        val domain = TestDataFactory.createAbsence()
        coEvery { absenceDao.insertAbsence(any()) } returns Unit

        // Act
        repository.insertAbsence(domain)

        // Assert
        coVerify { absenceDao.insertAbsence(any()) }
    }

    @Test
    fun `when deleteAbsence, then dao deleteAbsence is called`() = runTest {
        // Arrange
        val domain = TestDataFactory.createAbsence()
        coEvery { absenceDao.deleteAbsence(any()) } returns Unit

        // Act
        repository.deleteAbsence(domain)

        // Assert
        coVerify { absenceDao.deleteAbsence(any()) }
    }

    @Test
    fun `when deleteById, then dao deleteById is called`() = runTest {
        // Arrange
        val id = 1L
        coEvery { absenceDao.deleteById(id) } returns Unit

        // Act
        repository.deleteById(id)

        // Assert
        coVerify { absenceDao.deleteById(id) }
    }
}
