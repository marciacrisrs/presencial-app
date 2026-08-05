package com.presencial.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_summaries")
data class MonthlySummaryEntity(
    @PrimaryKey val yearMonthKey: String,
    val workdays: Int,
    val requiredDays: Int,
    val completedDays: Int,
    val homeOfficeDays: Int,
    val requiredPercentage: Int,
    val achievedPercentage: Float
)
