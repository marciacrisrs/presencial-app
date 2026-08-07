package com.presencial.app.data.repository

import app.cash.turbine.test
import com.presencial.app.data.local.dao.CheckInDao
import com.presencial.app.data.local.dao.MonthlySummaryDao
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.YearMonth
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MonthlySummaryRepositoryImplTest {

    private val monthlySummaryDao: MonthlySummaryDao = mockk()
    private val checkInDao: CheckInDao = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private lateinit var repository: MonthlySummaryRepositoryImpl

    @BeforeEach
    fun setup() {
        repository = MonthlySummaryRepositoryImpl(
            monthlySummaryDao,
            checkInDao,
            settingsRepository
        )
    }

    @Test
    fun `when observeAllSummaries, then return domain list from dao`() = runTest {
        // Arrange
        val entities = listOf(TestDataFactory.createMonthlySummaryEntity())
        every { monthlySummaryDao.observeAll() } returns flowOf(entities)

        // Act & Assert
        repository.observeAllSummaries().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(entities[0].yearMonthKey, result[0].yearMonth.toString())
            awaitComplete()
        }
    }

    @Test
    fun `when getSummary, then return domain object from dao`() = runTest {
        // Arrange
        val yearMonth = YearMonth.of(2026, 8)
        val entity = TestDataFactory.createMonthlySummaryEntity(yearMonthKey = yearMonth.toString())
        coEvery { monthlySummaryDao.getByKey(yearMonth.toString()) } returns entity

        // Act
        val result = repository.getSummary(yearMonth)

        // Assert
        assertEquals(yearMonth, result?.yearMonth)
    }

    @Test
    fun `when saveSummary, then dao upsert is called`() = runTest {
        // Arrange
        val summary = TestDataFactory.createMonthlySummary()
        coEvery { monthlySummaryDao.upsert(any()) } returns Unit

        // Act
        repository.saveSummary(summary)

        // Assert
        coVerify { monthlySummaryDao.upsert(any()) }
    }

    @Test
    fun `when refreshSummary, then calculate and save new summary`() = runTest {
        // Arrange
        val yearMonth = YearMonth.of(2026, 8)
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = false)
        every { settingsRepository.settings } returns flowOf(settings)
        
        val checkIns = listOf(
            TestDataFactory.createCheckInEntity(status = "PRESENCIAL"),
            TestDataFactory.createCheckInEntity(status = "HOME_OFFICE")
        )
        coEvery { checkInDao.getBetween(any(), any()) } returns checkIns
        coEvery { monthlySummaryDao.upsert(any()) } returns Unit

        // Act
        repository.refreshSummary(yearMonth)

        // Assert
        coVerify { monthlySummaryDao.upsert(match { 
            it.completedDays == 1 && it.homeOfficeDays == 1 && it.yearMonthKey == yearMonth.toString()
        }) }
    }
}
