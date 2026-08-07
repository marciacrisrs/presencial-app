package com.presencial.app.data.repository

import app.cash.turbine.test
import com.presencial.app.data.local.dao.WorkAddressDao
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

    @Test
    fun `when getActiveAddresses, then return domain list from dao`() = runTest {
        // Arrange
        val entities = listOf(TestDataFactory.createWorkAddressEntity(isActive = true))
        coEvery { workAddressDao.getActiveAddresses() } returns entities

        // Act
        val result = repository.getActiveAddresses()

        // Assert
        assertEquals(1, result.size)
        assertEquals(entities[0].name, result[0].name)
    }

    @Test
    fun `when insertAddress, then dao insertAddress is called`() = runTest {
        // Arrange
        val domain = TestDataFactory.createWorkAddress()
        coEvery { workAddressDao.insertAddress(any()) } returns Unit

        // Act
        repository.insertAddress(domain)

        // Assert
        coVerify { workAddressDao.insertAddress(any()) }
    }

    @Test
    fun `when updateAddress, then dao updateAddress is called`() = runTest {
        // Arrange
        val domain = TestDataFactory.createWorkAddress()
        coEvery { workAddressDao.updateAddress(any()) } returns Unit

        // Act
        repository.updateAddress(domain)

        // Assert
        coVerify { workAddressDao.updateAddress(any()) }
    }

    @Test
    fun `when deleteAddress, then dao deleteAddress is called`() = runTest {
        // Arrange
        val domain = TestDataFactory.createWorkAddress()
        coEvery { workAddressDao.deleteAddress(any()) } returns Unit

        // Act
        repository.deleteAddress(domain)

        // Assert
        coVerify { workAddressDao.deleteAddress(any()) }
    }

    @Test
    fun `when getAddressById, then return domain object from dao`() = runTest {
        // Arrange
        val id = 1L
        val entity = TestDataFactory.createWorkAddressEntity(id = id)
        coEvery { workAddressDao.getAddressById(id) } returns entity

        // Act
        val result = repository.getAddressById(id)

        // Assert
        assertEquals(id, result?.id)
        assertEquals(entity.name, result?.name)
    }
}
