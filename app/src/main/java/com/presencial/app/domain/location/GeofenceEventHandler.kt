package com.presencial.app.domain.location

import com.presencial.app.domain.usecase.AutoCheckInResult
import com.presencial.app.domain.usecase.AutoGeofenceCheckInUseCase
import com.presencial.app.notification.NotificationHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceEventHandler @Inject constructor(
    private val autoGeofenceCheckInUseCase: AutoGeofenceCheckInUseCase,
    private val notificationHelper: NotificationHelper
) {
    suspend fun handleDwellTransition(workAddressId: Long?): AutoCheckInResult {
        val result = autoGeofenceCheckInUseCase(workAddressId)
        if (result == AutoCheckInResult.Success) {
            notificationHelper.showAutoCheckInNotification()
        }
        return result
    }
}
