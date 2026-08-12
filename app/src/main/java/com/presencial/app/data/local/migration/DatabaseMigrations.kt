package com.presencial.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE check_ins ADD COLUMN workAddressId INTEGER DEFAULT NULL"
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE work_addresses ADD COLUMN stateCode TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE work_addresses ADD COLUMN cityName TEXT DEFAULT NULL")
    }
}
