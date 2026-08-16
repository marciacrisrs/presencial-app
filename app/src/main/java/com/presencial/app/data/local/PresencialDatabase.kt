package com.presencial.app.data.local

import android.content.Context
import androidx.annotation.VisibleForTesting
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
import com.presencial.app.data.local.migration.ALL_MIGRATIONS

@Database(
    entities = [CheckInEntity::class, MonthlySummaryEntity::class, AbsenceEntity::class, WorkAddressEntity::class],
    version = 5,
    exportSchema = false
)
abstract class PresencialDatabase : RoomDatabase() {
    abstract fun checkInDao(): CheckInDao
    abstract fun monthlySummaryDao(): MonthlySummaryDao
    abstract fun absenceDao(): AbsenceDao
    abstract fun workAddressDao(): WorkAddressDao

    companion object {
        const val NAME = "presencial.db"

        @Volatile
        private var INSTANCE: PresencialDatabase? = null

        fun getInstance(context: Context): PresencialDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: create(context.applicationContext, NAME).also { INSTANCE = it }
            }
        }

        /**
         * App (Hilt) e widget usam [getInstance]. Sem fallback destrutivo.
         */
        @VisibleForTesting
        fun create(context: Context, name: String): PresencialDatabase =
            Room.databaseBuilder(context, PresencialDatabase::class.java, name)
                .addMigrations(*ALL_MIGRATIONS)
                .build()
    }
}
