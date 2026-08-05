package com.presencial.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.presencial.app.data.local.dao.CheckInDao
import com.presencial.app.data.local.dao.MonthlySummaryDao
import com.presencial.app.data.local.entity.CheckInEntity
import com.presencial.app.data.local.entity.MonthlySummaryEntity

@Database(
    entities = [CheckInEntity::class, MonthlySummaryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PresencialDatabase : RoomDatabase() {
    abstract fun checkInDao(): CheckInDao
    abstract fun monthlySummaryDao(): MonthlySummaryDao

    companion object {
        @Volatile
        private var INSTANCE: PresencialDatabase? = null

        fun getInstance(context: Context): PresencialDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PresencialDatabase::class.java,
                    "presencial.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
