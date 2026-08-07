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
import java.time.YearMonth
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
    fun `when observeCheckInsForMonth, then return domain list from dao`() = runTest {
        // Arrange
        val yearMonth = YearMonth.of(2026, 8)
        val entities = listOf(TestDataFactory.createCheckInEntity(dateEpochDay = yearMonth.atDay(1).toEpochDay()))
        every { checkInDao.observeBetween(any(), any()) } returns flowOf(entities)

        // Act & Assert
        repository.observeCheckInsForMonth(yearMonth).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            awaitComplete()
        }
    }

    @Test
    fun `when observeAllCheckIns, then return domain list from dao`() = runTest {
        // Arrange
        val entities = listOf(TestDataFactory.createCheckInEntity())
        every { checkInDao.observeAll() } returns flowOf(entities)

        // Act & Assert
        repository.observeAllCheckIns().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            awaitComplete()
        }
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

    @Test
    fun `when deleteCheckIn, then dao deleteByDate is called`() = runTest {
        // Arrange
        val date = LocalDate.of(2026, 8, 6)
        coEvery { checkInDao.deleteByDate(date.toEpochDay()) } returns Unit

        // Act
        repository.deleteCheckIn(date)

        // Assert
        coVerify { checkInDao.deleteByDate(date.toEpochDay()) }
    }

    @Test
    fun `when getCheckInsBetween, then return domain list from dao`() = runTest {
        // Arrange
        val start = LocalDate.of(2026, 8, 1)
        val end = LocalDate.of(2026, 8, 31)
        val entities = listOf(TestDataFactory.createCheckInEntity())
        coEvery { checkInDao.getBetween(start.toEpochDay(), end.toEpochDay()) } returns entities

        // Act
        val result = repository.getCheckInsBetween(start, end)

        // Assert
        assertEquals(1, result.size)
    }
}
