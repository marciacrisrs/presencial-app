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
    fun `when refreshSummary, then calculate and save new summary`() = runTest {
        // Arrange
        val yearMonth = YearMonth.of(2026, 8)
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = false)
        every { settingsRepository.settings } returns flowOf(settings)
        coEvery { checkInDao.getBetween(any(), any()) } returns emptyList()
        coEvery { monthlySummaryDao.upsert(any()) } returns Unit

        // Act
        repository.refreshSummary(yearMonth)

        // Assert
        coVerify { monthlySummaryDao.upsert(any()) }
    }
}
