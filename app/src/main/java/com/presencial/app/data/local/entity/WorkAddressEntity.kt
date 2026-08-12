package com.presencial.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_addresses")
data class WorkAddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val addressText: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float = 50f,
    val isActive: Boolean = true,
    val stateCode: String? = null,
    val cityName: String? = null
)
