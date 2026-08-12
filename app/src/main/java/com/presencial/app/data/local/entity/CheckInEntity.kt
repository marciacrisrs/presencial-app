package com.presencial.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_ins")
data class CheckInEntity(
    @PrimaryKey val dateEpochDay: Long,
    val status: String,
    val updatedAt: Long,
    val source: String = "MANUAL",
    val workAddressId: Long? = null
)
