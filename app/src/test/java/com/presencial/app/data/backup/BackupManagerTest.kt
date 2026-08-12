package com.presencial.app.data.backup

import com.presencial.app.data.local.dao.CheckInDao
import com.presencial.app.data.local.dao.MonthlySummaryDao
import com.presencial.app.data.local.dao.WorkAddressDao
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.CheckInSource
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
    private val workAddressDao: WorkAddressDao = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val ioDispatcher = Dispatchers.IO

    private lateinit var backupManager: BackupManager

    @BeforeEach
    fun setup() {
        backupManager = BackupManager(
            checkInDao,
            monthlySummaryDao,
            workAddressDao,
            settingsRepository,
            ioDispatcher
        )
    }

    @Test
    fun `when exportToStream, then write JSON with work addresses and source`() = runTest {
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = false)
        every { settingsRepository.settings } returns flowOf(settings)
        every { checkInDao.observeAll() } returns flowOf(
            listOf(
                TestDataFactory.createCheckInEntity(
                    source = CheckInSource.AUTO_GEOFENCE
                )
            )
        )
        every { monthlySummaryDao.observeAll() } returns flowOf(listOf(TestDataFactory.createMonthlySummaryEntity()))
        coEvery { workAddressDao.getAllAddressesSync() } returns listOf(TestDataFactory.createWorkAddressEntity())

        val outputStream = ByteArrayOutputStream()
        val result = backupManager.exportToStream(outputStream)

        assertTrue(result.isSuccess)
        val jsonString = outputStream.toString()
        assertTrue(jsonString.contains("\"source\": \"${CheckInSource.AUTO_GEOFENCE}\""))
        assertTrue(jsonString.contains("\"workAddresses\":"))
    }

    @Test
    fun `when exportToStream fails to write, then return failure`() = runTest {
        val settings = AppSettings()
        every { settingsRepository.settings } returns flowOf(settings)
        every { checkInDao.observeAll() } returns flowOf(emptyList())
        every { monthlySummaryDao.observeAll() } returns flowOf(emptyList())
        coEvery { workAddressDao.getAllAddressesSync() } returns emptyList()

        val outputStream = mockk<java.io.OutputStream>()
        every { outputStream.write(any<ByteArray>()) } throws java.io.IOException("Disk full")
        every { outputStream.close() } returns Unit

        val result = backupManager.exportToStream(outputStream)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is java.io.IOException)
    }

    @Test
    fun `when importFromFile with invalid JSON, then return failure`() = runTest {
        val tempFile = File.createTempFile("invalid_backup", ".json")
        tempFile.writeText("invalid json")

        val result = backupManager.importFromFile(tempFile)

        assertTrue(result.isFailure)
        tempFile.delete()
    }

    @Test
    fun `when importFromFile, then restore check-ins work addresses and settings`() = runTest {
        val json = """
            {
              "version": 2,
              "requiredPercentage": 60,
              "countSaturdaysAsWorkdays": true,
              "checkIns": [
                {
                  "dateEpochDay": 20672,
                  "status": "PRESENCIAL",
                  "updatedAt": 1723000000000,
                  "source": "auto_geofence",
                  "workAddressId": 3
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
              ],
              "workAddresses": [
                {
                  "id": 3,
                  "name": "Escritório",
                  "addressText": "Rua A",
                  "latitude": -23.55,
                  "longitude": -46.63,
                  "radius": 50.0,
                  "isActive": true
                }
              ]
            }
        """.trimIndent()

        val tempFile = File.createTempFile("backup_test", ".json")
        tempFile.writeText(json)

        coEvery { checkInDao.deleteAll() } returns Unit
        coEvery { monthlySummaryDao.deleteAll() } returns Unit
        coEvery { workAddressDao.deleteAll() } returns Unit
        coEvery { checkInDao.insertAll(any()) } returns Unit
        coEvery { monthlySummaryDao.insertAll(any()) } returns Unit
        coEvery { workAddressDao.insertAll(any()) } returns Unit
        coEvery { settingsRepository.updateRequiredPercentage(any()) } returns Unit
        coEvery { settingsRepository.updateCountSaturdaysAsWorkdays(any()) } returns Unit

        val result = backupManager.importFromFile(tempFile)

        assertTrue(result.isSuccess)
        coVerify { workAddressDao.deleteAll() }
        coVerify { workAddressDao.insertAll(match { it.size == 1 }) }
        coVerify { checkInDao.insertAll(match { it.first().source == CheckInSource.AUTO_GEOFENCE }) }
        tempFile.delete()
    }

    @Test
    fun `when importFromFile with missing fields, then return failure`() = runTest {
        val json = """
            {
              "version": 1,
              "requiredPercentage": 40
            }
        """.trimIndent()
        val tempFile = File.createTempFile("missing_fields", ".json")
        tempFile.writeText(json)

        val result = backupManager.importFromFile(tempFile)

        assertTrue(result.isFailure)
        tempFile.delete()
    }

    @Test
    fun `when importFromFile and dao fails, then return failure`() = runTest {
        val json = """
            {"version":2,"requiredPercentage":40,"countSaturdaysAsWorkdays":false,"checkIns":[],"summaries":[]}
        """.trimIndent()
        val tempFile = File.createTempFile("dao_fail", ".json")
        tempFile.writeText(json)

        coEvery { checkInDao.deleteAll() } throws RuntimeException("Database error")

        val result = backupManager.importFromFile(tempFile)

        assertTrue(result.isFailure)
        tempFile.delete()
    }
}
