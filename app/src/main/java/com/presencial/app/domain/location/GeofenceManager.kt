package com.presencial.app.domain.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.presencial.app.domain.model.WorkAddress
import com.presencial.app.worker.GeofenceBroadcastReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val geofencingClient = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getBroadcast(context, 0, intent, flags)
    }

    @SuppressLint("MissingPermission")
    fun registerGeofences(addresses: List<WorkAddress>) {
        if (addresses.isEmpty()) {
            removeGeofences()
            return
        }

        val geofences = addresses.filter { it.isActive }.map { address ->
            Geofence.Builder()
                .setRequestId(address.id.toString())
                .setCircularRegion(address.latitude, address.longitude, address.radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(30000) // 30 seconds
                .build()
        }

        if (geofences.isEmpty()) {
            removeGeofences()
            return
        }

        val request = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
            addGeofences(geofences)
        }.build()

        geofencingClient.addGeofences(request, geofencePendingIntent).addOnFailureListener {
            // Log or handle failure
        }
    }

    fun removeGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)
    }
}
