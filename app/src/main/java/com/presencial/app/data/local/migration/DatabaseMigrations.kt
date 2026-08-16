package com.presencial.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
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
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
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
        db.execSQL(
            "ALTER TABLE check_ins ADD COLUMN source TEXT NOT NULL DEFAULT 'MANUAL'"
        )
    }
}

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

val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
