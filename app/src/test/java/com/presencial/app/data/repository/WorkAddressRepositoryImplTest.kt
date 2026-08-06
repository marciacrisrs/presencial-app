package com.presencial.app.data.repository

import app.cash.turbine.test
import com.presencial.app.data.local.dao.WorkAddressDao
import com.presencial.app.util.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WorkAddressRepositoryImplTest {

    private val workAddressDao: WorkAddressDao = mockk()
    private lateinit var repository: WorkAddressRepositoryImpl

    @BeforeEach
    fun setup() {
        repository = WorkAddressRepositoryImpl(workAddressDao)
    }

    @Test
    fun `when getAllAddresses, then return domain list from dao`() = runTest {
        // Arrange
        val entities = listOf(TestDataFactory.createWorkAddressEntity())
        every { workAddressDao.getAllAddresses() } returns flowOf(entities)

        // Act & Assert
        repository.getAllAddresses().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(entities[0].name, result[0].name)
            awaitComplete()
        }
    }
}
