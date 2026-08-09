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
