package com.presencial.app.data.backup

import com.presencial.app.data.local.dao.AbsenceDao
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
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.model.WeekParity
import java.time.DayOfWeek
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File

class BackupManagerTest {

    private val checkInDao: CheckInDao = mockk()
    private val monthlySummaryDao: MonthlySummaryDao = mockk()
    private val workAddressDao: WorkAddressDao = mockk()
    private val absenceDao: AbsenceDao = mockk()
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
            settingsRepository,
            ioDispatcher
        )
        every { absenceDao.getAllAbsences() } returns flowOf(emptyList())
        coEvery { absenceDao.deleteAll() } returns Unit
        coEvery { absenceDao.insertAll(any()) } returns Unit
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

        coEvery { checkInDao.deleteAll() } returns Unit
        coEvery { monthlySummaryDao.deleteAll() } returns Unit
        coEvery { workAddressDao.deleteAll() } returns Unit
        coEvery { absenceDao.deleteAll() } returns Unit
        coEvery { checkInDao.insertAll(any()) } returns Unit
        coEvery { monthlySummaryDao.insertAll(any()) } returns Unit
        coEvery { settingsRepository.updateRequiredPercentage(35) } returns Unit
        coEvery { settingsRepository.updateCountSaturdaysAsWorkdays(false) } returns Unit
        coEvery { settingsRepository.updatePresencePolicy(any()) } returns Unit

        val result = backupManager.importFromFile(tempFile)

        assertTrue(result.isSuccess)
        coVerify { settingsRepository.updatePresencePolicy(match { it.companyName == "Acme" }) }
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

        val tempFile = File.createTempFile("backup_test", ".json")
        tempFile.writeText(json)

        coEvery { checkInDao.deleteAll() } returns Unit
        coEvery { monthlySummaryDao.deleteAll() } returns Unit
        coEvery { workAddressDao.deleteAll() } returns Unit
        coEvery { absenceDao.deleteAll() } returns Unit
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
            {"version":3,"requiredPercentage":40,"countSaturdaysAsWorkdays":false,"checkIns":[],"summaries":[]}
        """.trimIndent()
        val tempFile = File.createTempFile("dao_fail", ".json")
        tempFile.writeText(json)

        coEvery { checkInDao.deleteAll() } throws RuntimeException("Database error")

        val result = backupManager.importFromFile(tempFile)

        assertTrue(result.isFailure)
        tempFile.delete()
    }

    @Test
    fun `when importFromBytes v4, then restore absences`() = runTest {
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
        stubSuccessfulImport()

        val result = backupManager.importFromBytes(json.toByteArray())

        assertTrue(result.isSuccess)
        coVerify {
            absenceDao.insertAll(match { absences ->
                absences.size == 1 &&
                    absences.first().type == "VACATION" &&
                    absences.first().notes == "Férias"
            })
        }
    }

    @Test
    fun `when importFromBytes with invalid JSON, then return failure`() = runTest {
        val result = backupManager.importFromBytes("invalid json".toByteArray())

        assertTrue(result.isFailure)
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
        stubSuccessfulImport()

        val result = backupManager.importFromFile(tempFile)

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { absenceDao.insertAll(any()) }
        tempFile.delete()
    }

    private fun stubSuccessfulImport() {
        coEvery { checkInDao.deleteAll() } returns Unit
        coEvery { monthlySummaryDao.deleteAll() } returns Unit
        coEvery { workAddressDao.deleteAll() } returns Unit
        coEvery { absenceDao.deleteAll() } returns Unit
        coEvery { checkInDao.insertAll(any()) } returns Unit
        coEvery { monthlySummaryDao.insertAll(any()) } returns Unit
        coEvery { settingsRepository.updateRequiredPercentage(any()) } returns Unit
        coEvery { settingsRepository.updateCountSaturdaysAsWorkdays(any()) } returns Unit
        coEvery { absenceDao.insertAll(any()) } returns Unit
    }
}
