package com.presencial.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val requiredPercentage: Int,
    val countSaturdaysAsWorkdays: Boolean
)
