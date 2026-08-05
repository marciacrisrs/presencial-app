package com.presencial.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "absences")
data class AbsenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // VACATION, DAY_OFF, LICENSE, ABSENCE
    val startDateEpochDay: Long,
    val endDateEpochDay: Long,
    val isFullDay: Boolean,
    val hours: Float = 8f,
    val notes: String? = null,
    val isCounted: Boolean = false // If false, subtract from required workdays
)
