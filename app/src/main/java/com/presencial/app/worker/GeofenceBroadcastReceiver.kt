package com.presencial.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.presencial.app.domain.usecase.AutoCheckInResult
import com.presencial.app.domain.usecase.AutoGeofenceCheckInUseCase
import com.presencial.app.notification.NotificationHelper
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface GeofenceEntryPoint {
        fun autoGeofenceCheckInUseCase(): AutoGeofenceCheckInUseCase
        fun notificationHelper(): NotificationHelper
    }

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Geofence error code: ${geofencingEvent.errorCode}")
            return
        }

        if (geofencingEvent.geofenceTransition != Geofence.GEOFENCE_TRANSITION_DWELL) return

        val workAddressId = geofencingEvent.triggeringGeofences
            ?.firstOrNull()
            ?.requestId
            ?.toLongOrNull()

        val pendingResult = goAsync()
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            GeofenceEntryPoint::class.java
        )

        scope.launch {
            try {
                when (entryPoint.autoGeofenceCheckInUseCase()(workAddressId)) {
                    AutoCheckInResult.Success ->
                        entryPoint.notificationHelper().showAutoCheckInNotification()
                    AutoCheckInResult.SkippedAlreadyCheckedIn ->
                        Log.d(TAG, "Check-in automático ignorado: já registrado hoje")
                    AutoCheckInResult.SkippedNonWorkday ->
                        Log.d(TAG, "Check-in automático ignorado: dia não útil")
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "GeofenceReceiver"
    }
}
