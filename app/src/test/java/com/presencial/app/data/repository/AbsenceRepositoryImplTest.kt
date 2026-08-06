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
    fun `when insertAbsence, then dao insertAbsence is called`() = runTest {
        // Arrange
        val domain = TestDataFactory.createAbsence()
        coEvery { absenceDao.insertAbsence(any()) } returns Unit

        // Act
        repository.insertAbsence(domain)

        // Assert
        coVerify { absenceDao.insertAbsence(any()) }
    }
}
