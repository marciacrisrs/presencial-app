package com.presencial.app.data.repository

import app.cash.turbine.test
import com.presencial.app.data.local.dao.CheckInDao
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CheckInRepositoryImplTest {

    private val checkInDao: CheckInDao = mockk()
    private lateinit var repository: CheckInRepositoryImpl

    @BeforeEach
    fun setup() {
        repository = CheckInRepositoryImpl(checkInDao)
    }

    @Test
    fun `when getCheckIn, then return domain object from dao`() = runTest {
        // Arrange
        val date = LocalDate.of(2026, 8, 6)
        val entity = TestDataFactory.createCheckInEntity(dateEpochDay = date.toEpochDay())
        coEvery { checkInDao.getByDate(date.toEpochDay()) } returns entity

        // Act
        val result = repository.getCheckIn(date)

        // Assert
        assertEquals(date, result?.date)
    }

    @Test
    fun `when saveCheckIn, then dao upsert is called`() = runTest {
        // Arrange
        val date = LocalDate.of(2026, 8, 6)
        coEvery { checkInDao.upsert(any()) } returns Unit

        // Act
        repository.saveCheckIn(date, DayStatus.PRESENCIAL, "MANUAL")

        // Assert
        coVerify { checkInDao.upsert(any()) }
    }
}
