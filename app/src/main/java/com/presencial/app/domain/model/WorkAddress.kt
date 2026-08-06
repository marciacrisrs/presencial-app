package com.presencial.app.domain.model

data class WorkAddress(
    val id: Long = 0,
    val name: String,
    val addressText: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float = 50f,
    val isActive: Boolean = true
)
