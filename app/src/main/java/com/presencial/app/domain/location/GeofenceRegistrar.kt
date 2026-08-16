package com.presencial.app.domain.location

import com.presencial.app.domain.model.WorkAddress

interface GeofenceRegistrar {
    suspend fun registerGeofences(addresses: List<WorkAddress>)

    suspend fun removeGeofences()
}
