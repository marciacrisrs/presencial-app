package com.presencial.app.data.backup

import com.presencial.app.data.local.dao.AbsenceDao
import com.presencial.app.data.local.dao.BackupDao
import com.presencial.app.data.local.dao.CheckInDao
import com.presencial.app.data.local.dao.MonthlySummaryDao
import com.presencial.app.data.local.dao.WorkAddressDao
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.CheckInSource
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.model.WeekParity
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
import java.time.DayOfWeek

class BackupManagerTest {

    private val checkInDao: CheckInDao = mockk()
    private val monthlySummaryDao: MonthlySummaryDao = mockk()
    private val workAddressDao: WorkAddressDao = mockk()
    private val absenceDao: AbsenceDao = mockk()
    private val backupDao: BackupDao = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val ioDispatcher = Dispatchers.IO

    private lateinit var backupManager: BackupManager

    @BeforeEach
    fun setup() {
        backupManager = BackupManager(
            checkInDao,
            monthlySummaryDao,
            workAddressDao,
            absenceDao,
            backupDao,
            settingsRepository,
            ioDispatcher
        )
        every { absenceDao.getAllAbsences() } returns flowOf(emptyList())
    }

    @Test
    fun `when exportToStream, then write JSON with work addresses and source`() = runTest {
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = false)
        every { settingsRepository.settings } returns flowOf(settings)
        every { checkInDao.observeAll() } returns flowOf(
            listOf(TestDataFactory.createCheckInEntity(source = CheckInSource.AUTO_GEOFENCE))
        )
        every { monthlySummaryDao.observeAll() } returns flowOf(
            listOf(TestDataFactory.createMonthlySummaryEntity())
        )
        coEvery { workAddressDao.getAllAddressesSync() } returns listOf(
            TestDataFactory.createWorkAddressEntity()
        )

        val outputStream = ByteArrayOutputStream()
        val result = backupManager.exportToStream(outputStream)

        assertTrue(result.isSuccess)
        val jsonString = outputStream.toString()
        assertTrue(jsonString.contains("\"source\": \"${CheckInSource.AUTO_GEOFENCE}\""))
        assertTrue(jsonString.contains("\"workAddresses\":"))
    }

    @Test
    fun `when exportToStream, then include presence policy v4`() = runTest {
        val policy = PresencePolicy(
            companyName = "Acme",
            freePercentageEnabled = true,
            freePercentage = 35,
            fixedWeekdaysEnabled = true,
            mandatoryWeekdays = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
            alternatingWeeksEnabled = true,
            onSiteWeekParity = WeekParity.EVEN
        )
        val settings = AppSettings(
            requiredPercentage = 35,
            countSaturdaysAsWorkdays = false,
            presencePolicy = policy
        )
        every { settingsRepository.settings } returns flowOf(settings)
        every { checkInDao.observeAll() } returns flowOf(emptyList())
        every { monthlySummaryDao.observeAll() } returns flowOf(emptyList())
        coEvery { workAddressDao.getAllAddressesSync() } returns emptyList()

        val outputStream = ByteArrayOutputStream()
        val result = backupManager.exportToStream(outputStream)

        assertTrue(result.isSuccess)
        val jsonString = outputStream.toString()
        assertTrue(jsonString.contains("\"version\": 4"))
        assertTrue(jsonString.contains("\"presencePolicy\""))
        assertTrue(jsonString.contains("\"companyName\": \"Acme\""))
    }

    @Test
    fun `when importFromFile v3, then restore presence policy`() = runTest {
        val json = """
            {
              "version": 3,
              "requiredPercentage": 35,
              "countSaturdaysAsWorkdays": false,
              "presencePolicy": {
                "companyName": "Acme",
                "freePercentageEnabled": true,
                "freePercentage": 35,
                "fixedWeekdaysEnabled": true,
                "mandatoryWeekdays": "TUESDAY,THURSDAY",
                "alternatingWeeksEnabled": false,
                "alternatingAnchorDate": 20672,
                "onSiteWeekParity": "EVEN",
                "conflictPriority": "UNION_MAX"
              },
              "checkIns": [],
              "summaries": []
            }
        """.trimIndent()
        val tempFile = File.createTempFile("backup_v3", ".json")
        tempFile.writeText(json)
        stubSuccessfulImport(AppSettings(requiredPercentage = 40))

        val result = backupManager.importFromFile(tempFile)

        assertTrue(result.isSuccess)
        coVerify {
            settingsRepository.restoreBackupSettings(
                requiredPercentage = 35,
                countSaturdaysAsWorkdays = false,
                presencePolicy = match { it.companyName == "Acme" }
            )
        }
        coVerify { backupDao.restoreAll(emptyList(), emptyList(), emptyList(), emptyList()) }
        tempFile.delete()
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
    fun `when importFromFile with invalid JSON, then return failure without touching database`() = runTest {
        val tempFile = File.createTempFile("invalid_backup", ".json")
        tempFile.writeText("invalid json")

        val result = backupManager.importFromFile(tempFile)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { backupDao.restoreAll(any(), any(), any(), any()) }
        tempFile.delete()
    }

    @Test
    fun `when importFromFile, then restore check-ins work addresses and settings`() = runTest {
        val tempFile = File.createTempFile("backup_test", ".json")
        tempFile.writeText(createBackupWithCheckInsAndAddress())
        stubSuccessfulImport(AppSettings(requiredPercentage = 40))

        val result = backupManager.importFromFile(tempFile)

        assertTrue(result.isSuccess)
        coVerify {
            backupDao.restoreAll(
                match { it.size == 1 && it.first().source == CheckInSource.AUTO_GEOFENCE },
                match { it.size == 1 },
                match { it.size == 1 && it.first().name == "Escritório" },
                match { it.isEmpty() }
            )
        }
        coVerify {
            settingsRepository.restoreBackupSettings(
                requiredPercentage = 60,
                countSaturdaysAsWorkdays = true,
                presencePolicy = null
            )
        }
        tempFile.delete()
    }

    @Test
    fun `when exportToBytes, then return JSON payload`() = runTest {
        val settings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = false)
        every { settingsRepository.settings } returns flowOf(settings)
        every { checkInDao.observeAll() } returns flowOf(emptyList())
        every { monthlySummaryDao.observeAll() } returns flowOf(emptyList())
        coEvery { workAddressDao.getAllAddressesSync() } returns emptyList()

        val result = backupManager.exportToBytes()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.decodeToString()?.contains("\"version\": 4") == true)
        assertTrue(result.getOrNull()?.decodeToString()?.contains("\"absences\"") == true)
    }

    @Test
    fun `when exportToStream, then include absences in v4 backup`() = runTest {
        val settings = AppSettings()
        every { settingsRepository.settings } returns flowOf(settings)
        every { checkInDao.observeAll() } returns flowOf(emptyList())
        every { monthlySummaryDao.observeAll() } returns flowOf(emptyList())
        coEvery { workAddressDao.getAllAddressesSync() } returns emptyList()
        every { absenceDao.getAllAbsences() } returns flowOf(listOf(TestDataFactory.createAbsenceEntity()))

        val outputStream = ByteArrayOutputStream()
        val result = backupManager.exportToStream(outputStream)

        assertTrue(result.isSuccess)
        assertTrue(outputStream.toString().contains("\"absences\""))
    }

    @Test
    fun `when importFromFile with unsupported version, then return failure`() = runTest {
        val tempFile = File.createTempFile("backup_v2", ".json")
        tempFile.writeText(
            """
            {
              "version": 2,
              "requiredPercentage": 40,
              "countSaturdaysAsWorkdays": false,
              "checkIns": [],
              "summaries": []
            }
            """.trimIndent()
        )

        val result = backupManager.importFromFile(tempFile)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { backupDao.restoreAll(any(), any(), any(), any()) }
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
        coVerify(exactly = 0) { backupDao.restoreAll(any(), any(), any(), any()) }
        tempFile.delete()
    }

    @Test
    fun `when importFromFile and dao fails, then return failure and restore previous settings`() = runTest {
        val previousSettings = AppSettings(requiredPercentage = 40, countSaturdaysAsWorkdays = false)
        val json = """
            {"version":3,"requiredPercentage":60,"countSaturdaysAsWorkdays":true,"checkIns":[],"summaries":[]}
        """.trimIndent()
        val tempFile = File.createTempFile("dao_fail", ".json")
        tempFile.writeText(json)

        every { settingsRepository.settings } returns flowOf(previousSettings)
        coEvery { settingsRepository.restoreBackupSettings(any(), any(), any()) } returns Unit
        coEvery { backupDao.restoreAll(any(), any(), any(), any()) } throws RuntimeException("Database error")

        val result = backupManager.importFromFile(tempFile)

        assertTrue(result.isFailure)
        coVerify(exactly = 1) {
            settingsRepository.restoreBackupSettings(
                requiredPercentage = 60,
                countSaturdaysAsWorkdays = true,
                presencePolicy = null
            )
        }
        coVerify(exactly = 1) {
            settingsRepository.restoreBackupSettings(
                requiredPercentage = previousSettings.requiredPercentage,
                countSaturdaysAsWorkdays = previousSettings.countSaturdaysAsWorkdays,
                presencePolicy = previousSettings.presencePolicy
            )
        }
        tempFile.delete()
    }

    @Test
    fun `when importFromBytes v4, then restore absences atomically`() = runTest {
        val json = """
            {
              "version": 4,
              "requiredPercentage": 40,
              "countSaturdaysAsWorkdays": false,
              "checkIns": [],
              "summaries": [],
              "absences": [
                {
                  "id": 1,
                  "type": "VACATION",
                  "startDateEpochDay": 20672,
                  "endDateEpochDay": 20676,
                  "isFullDay": true,
                  "hours": 8.0,
                  "notes": "Férias",
                  "isCounted": false
                }
              ]
            }
        """.trimIndent()
        stubSuccessfulImport(AppSettings(requiredPercentage = 40))

        val result = backupManager.importFromBytes(json.toByteArray())

        assertTrue(result.isSuccess)
        coVerify {
            backupDao.restoreAll(
                match { it.isEmpty() },
                match { it.isEmpty() },
                match { it.isEmpty() },
                match {
                    it.size == 1 &&
                        it.first().type == "VACATION" &&
                        it.first().notes == "Férias"
                }
            )
        }
    }

    @Test
    fun `when importFromBytes with invalid JSON, then return failure`() = runTest {
        val result = backupManager.importFromBytes("invalid json".toByteArray())

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { backupDao.restoreAll(any(), any(), any(), any()) }
    }

    @Test
    fun `when importFromBytes with unsupported version, then return failure`() = runTest {
        val json = """
            {
              "version": 2,
              "requiredPercentage": 40,
              "countSaturdaysAsWorkdays": false,
              "checkIns": [],
              "summaries": []
            }
        """.trimIndent()

        val result = backupManager.importFromBytes(json.toByteArray())

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { backupDao.restoreAll(any(), any(), any(), any()) }
    }

    @Test
    fun `when importFromFile v4 without absences key, then skip absence restore`() = runTest {
        val json = """
            {
              "version": 4,
              "requiredPercentage": 40,
              "countSaturdaysAsWorkdays": false,
              "checkIns": [],
              "summaries": []
            }
        """.trimIndent()
        val tempFile = File.createTempFile("backup_v4_no_absences", ".json")
        tempFile.writeText(json)
        stubSuccessfulImport(AppSettings(requiredPercentage = 40))

        val result = backupManager.importFromFile(tempFile)

        assertTrue(result.isSuccess)
        coVerify {
            backupDao.restoreAll(
                match { it.isEmpty() },
                match { it.isEmpty() },
                match { it.isEmpty() },
                match { it.isEmpty() }
            )
        }
        tempFile.delete()
    }

    @Test
    fun `when restore payload is invalid, then current data is not touched`() = runTest {
        val json = """
            {
              "version": 4,
              "requiredPercentage": 40,
              "countSaturdaysAsWorkdays": false,
              "checkIns": [
                {"dateEpochDay": 20672}
              ],
              "summaries": []
            }
        """.trimIndent()

        val result = backupManager.importFromBytes(json.toByteArray())

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { backupDao.restoreAll(any(), any(), any(), any()) }
    }

    private fun stubSuccessfulImport(previousSettings: AppSettings) {
        every { settingsRepository.settings } returns flowOf(previousSettings)
        coEvery { backupDao.restoreAll(any(), any(), any(), any()) } returns Unit
        coEvery { settingsRepository.restoreBackupSettings(any(), any(), any()) } returns Unit
    }

    private fun createBackupWithCheckInsAndAddress(): String = """
        {
          "version": 3,
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
}
