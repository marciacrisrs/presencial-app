package com.presencial.app.data.backup

import com.presencial.app.data.local.dao.CheckInDao
import com.presencial.app.data.local.dao.MonthlySummaryDao
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File

class BackupManagerTest {

    private val checkInDao: CheckInDao = mockk()
    private val monthlySummaryDao: MonthlySummaryDao = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val ioDispatcher = Dispatchers.IO

    private lateinit var backupManager: BackupManager

    @BeforeEach
    fun setup() {
        backupManager = BackupManager(checkInDao, monthlySummaryDao, settingsRepository, ioDispatcher)
    }

    @Test
    fun `when exportToStream, then write JSON to stream`() = runTest {
        // Arrange
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = false)
        every { settingsRepository.settings } returns flowOf(settings)
        every { checkInDao.observeAll() } returns flowOf(listOf(TestDataFactory.createCheckInEntity()))
        every { monthlySummaryDao.observeAll() } returns flowOf(listOf(TestDataFactory.createMonthlySummaryEntity()))
        
        val outputStream = ByteArrayOutputStream()

        // Act
        val result = backupManager.exportToStream(outputStream)

        // Assert
        assertTrue(result.isSuccess)
        val jsonString = outputStream.toString()
        assertTrue(jsonString.contains("\"requiredPercentage\": 40"))
        assertTrue(jsonString.contains("\"checkIns\":"))
        assertTrue(jsonString.contains("\"summaries\":"))
    }

    @Test
    fun `when exportToStream fails to write, then return failure`() = runTest {
        // Arrange
        val settings = AppSettings()
        every { settingsRepository.settings } returns flowOf(settings)
        every { checkInDao.observeAll() } returns flowOf(emptyList())
        every { monthlySummaryDao.observeAll() } returns flowOf(emptyList())
        
        val outputStream = mockk<java.io.OutputStream>()
        every { outputStream.write(any<ByteArray>()) } throws java.io.IOException("Disk full")
        every { outputStream.close() } returns Unit

        // Act
        val result = backupManager.exportToStream(outputStream)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is java.io.IOException)
    }

    @Test
    fun `when importFromFile with invalid JSON, then return failure`() = runTest {
        // Arrange
        val tempFile = File.createTempFile("invalid_backup", ".json")
        tempFile.writeText("invalid json")
        
        // Act
        val result = backupManager.importFromFile(tempFile)

        // Assert
        assertTrue(result.isFailure)
        tempFile.delete()
    }

    @Test
    fun `when importFromFile, then call daos and repository`() = runTest {
        // Arrange
        val json = """
            {
              "version": 1,
              "requiredPercentage": 60,
              "countSaturdaysAsWorkdays": true,
              "checkIns": [
                {
                  "dateEpochDay": 20672,
                  "status": "PRESENCIAL",
                  "updatedAt": 1723000000000
                }
              ],
              "summaries": [
                {
                  "yearMonthKey": "2026-08",
                  "workdays": 21,
                  "requiredDays": 9,
                  "completedDays": 5,
                  "homeOfficeDays": 2,
                  "requiredPercentage": 40,
                  "achievedPercentage": 55.5
                }
              ]
            }
        """.trimIndent()
        
        val tempFile = File.createTempFile("backup_test", ".json")
        tempFile.writeText(json)
        
        coEvery { checkInDao.deleteAll() } returns Unit
        coEvery { monthlySummaryDao.deleteAll() } returns Unit
        coEvery { checkInDao.insertAll(any()) } returns Unit
        coEvery { monthlySummaryDao.insertAll(any()) } returns Unit
        coEvery { settingsRepository.updateRequiredPercentage(any()) } returns Unit
        coEvery { settingsRepository.updateCountSaturdaysAsWorkdays(any()) } returns Unit

        // Act
        val result = backupManager.importFromFile(tempFile)

        // Assert
        assertTrue(result.isSuccess)
        coVerify { checkInDao.deleteAll() }
        coVerify { monthlySummaryDao.deleteAll() }
        coVerify { checkInDao.insertAll(match { it.size == 1 }) }
        coVerify { monthlySummaryDao.insertAll(match { it.size == 1 }) }
        coVerify { settingsRepository.updateRequiredPercentage(60) }
        coVerify { settingsRepository.updateCountSaturdaysAsWorkdays(true) }
        
        tempFile.delete()
    }
}
