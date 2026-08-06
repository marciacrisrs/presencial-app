package com.presencial.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.presencial.app.data.local.dao.AbsenceDao
import com.presencial.app.data.local.dao.CheckInDao
import com.presencial.app.data.local.dao.MonthlySummaryDao
import com.presencial.app.data.local.dao.WorkAddressDao
import com.presencial.app.data.local.entity.AbsenceEntity
import com.presencial.app.data.local.entity.CheckInEntity
import com.presencial.app.data.local.entity.MonthlySummaryEntity
import com.presencial.app.data.local.entity.WorkAddressEntity

@Database(
    entities = [CheckInEntity::class, MonthlySummaryEntity::class, AbsenceEntity::class, WorkAddressEntity::class],
    version = 3,
    exportSchema = false
)
abstract class PresencialDatabase : RoomDatabase() {
    abstract fun checkInDao(): CheckInDao
    abstract fun monthlySummaryDao(): MonthlySummaryDao
    abstract fun absenceDao(): AbsenceDao
    abstract fun workAddressDao(): WorkAddressDao

    companion object {
        @Volatile
        private var INSTANCE: PresencialDatabase? = null

        fun getInstance(context: Context): PresencialDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = INSTANCE
                if (instance != null) {
                    instance
                } else {
                    val newInstance = Room.databaseBuilder(
                        context.applicationContext,
                        PresencialDatabase::class.java,
                        "presencial.db"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = newInstance
                    newInstance
                }
            }
        }
    }
}
