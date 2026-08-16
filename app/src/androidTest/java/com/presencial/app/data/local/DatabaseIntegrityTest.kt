package com.presencial.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.presencial.app.data.local.dao.AbsenceDao
import com.presencial.app.data.local.dao.CheckInDao
import com.presencial.app.data.local.dao.WorkAddressDao
import com.presencial.app.data.local.entity.AbsenceEntity
import com.presencial.app.data.local.entity.CheckInEntity
import com.presencial.app.data.local.entity.WorkAddressEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseIntegrityTest {

    private lateinit var db: PresencialDatabase
    private lateinit var checkInDao: CheckInDao
    private lateinit var absenceDao: AbsenceDao
    private lateinit var workAddressDao: WorkAddressDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PresencialDatabase::class.java).build()
        checkInDao = db.checkInDao()
        absenceDao = db.absenceDao()
        workAddressDao = db.workAddressDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun appAndWidgetShareTheSameDatabaseInstance() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val fromApp = PresencialDatabase.getInstance(context)
        val fromWidget = PresencialDatabase.getInstance(context)
        assertSame(fromApp, fromWidget)
    }

    @Test
    fun migrationFromVersion4KeepsCheckInsAndAddsLocationColumns() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migration_4_5.db"
        context.deleteDatabase(name)

        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { sqlite ->
            sqlite.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `check_ins` (
                    `dateEpochDay` INTEGER NOT NULL,
                    `status` TEXT NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `source` TEXT NOT NULL,
                    `workAddressId` INTEGER,
                    PRIMARY KEY(`dateEpochDay`)
                )
                """.trimIndent()
            )
            sqlite.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `monthly_summaries` (
                    `yearMonthKey` TEXT NOT NULL,
                    `workdays` INTEGER NOT NULL,
                    `requiredDays` INTEGER NOT NULL,
                    `completedDays` INTEGER NOT NULL,
                    `homeOfficeDays` INTEGER NOT NULL,
                    `requiredPercentage` INTEGER NOT NULL,
                    `achievedPercentage` REAL NOT NULL,
                    PRIMARY KEY(`yearMonthKey`)
                )
                """.trimIndent()
            )
            sqlite.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `absences` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `type` TEXT NOT NULL,
                    `startDateEpochDay` INTEGER NOT NULL,
                    `endDateEpochDay` INTEGER NOT NULL,
                    `isFullDay` INTEGER NOT NULL,
                    `hours` REAL NOT NULL,
                    `notes` TEXT,
                    `isCounted` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            sqlite.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `work_addresses` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `addressText` TEXT NOT NULL,
                    `latitude` REAL NOT NULL,
                    `longitude` REAL NOT NULL,
                    `radius` REAL NOT NULL,
                    `isActive` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            sqlite.execSQL(
                "INSERT INTO check_ins (dateEpochDay, status, updatedAt, source) VALUES (12345, 'PRESENCIAL', 1000, 'MANUAL')"
            )
            sqlite.execSQL(
                "INSERT INTO work_addresses (name, addressText, latitude, longitude, radius, isActive) VALUES ('Escritorio', 'Rua 1', 1.0, 2.0, 50.0, 1)"
            )
            sqlite.execSQL("PRAGMA user_version = 4")
        }

        val migrated = PresencialDatabase.create(context, name)
        try {
            val checkIn = migrated.checkInDao().getByDate(12345L)
            assertEquals("PRESENCIAL", checkIn?.status)
            val addresses = migrated.workAddressDao().getAllAddresses().first()
            assertEquals(1, addresses.size)
            assertEquals(null, addresses[0].stateCode)
            assertEquals(null, addresses[0].cityName)
        } finally {
            migrated.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun insertAndReadCheckIn() = runBlocking {
        val checkIn = CheckInEntity(dateEpochDay = 12345L, status = "PRESENT", updatedAt = 1000L)
        checkInDao.upsert(checkIn)
        val result = checkInDao.getByDate(12345L)
        assertEquals(checkIn, result)
    }

    @Test
    fun insertAndReadAbsence() = runBlocking {
        val absence = AbsenceEntity(
            type = "VACATION",
            startDateEpochDay = 100L,
            endDateEpochDay = 110L,
            isFullDay = true
        )
        absenceDao.insertAbsence(absence)
        val result = absenceDao.getAllAbsences().first()
        assertEquals(1, result.size)
        assertEquals(absence.type, result[0].type)
        assertEquals(absence.startDateEpochDay, result[0].startDateEpochDay)
    }

    @Test
    fun insertAndReadWorkAddress() = runBlocking {
        val address = WorkAddressEntity(
            name = "Home",
            addressText = "Street 1",
            latitude = 1.0,
            longitude = 2.0
        )
        workAddressDao.insertAddress(address)
        val result = workAddressDao.getAllAddresses().first()
        assertEquals(1, result.size)
        assertEquals(address.name, result[0].name)
        assertEquals(address.latitude, result[0].latitude, 0.0)
    }
}
