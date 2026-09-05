package com.presencial.app.data.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.presencial.app.domain.location.GeofenceRegistrar
import com.presencial.app.domain.location.GeofenceRegistrationException
import com.presencial.app.domain.model.WorkAddress
import com.presencial.app.worker.GeofenceBroadcastReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidGeofenceRegistrar @Inject constructor(
    @ApplicationContext private val context: Context
) : GeofenceRegistrar {

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
    override suspend fun registerGeofences(addresses: List<WorkAddress>) {
        try {
            if (addresses.isEmpty()) {
                removeGeofences()
                return
            }

            val geofences = addresses.filter { it.isActive }.map { address ->
                Geofence.Builder()
                    .setRequestId(address.id.toString())
                    .setCircularRegion(address.latitude, address.longitude, address.radius)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(CHECK_IN_TRANSITIONS)
                    .setLoiteringDelay(LOITERING_DELAY_MS.toInt())
                    .build()
            }

            if (geofences.isEmpty()) {
                removeGeofences()
                return
            }

            val request = GeofencingRequest.Builder().apply {
                setInitialTrigger(INITIAL_TRIGGERS)
                addGeofences(geofences)
            }.build()

            // Replace the complete set so every retry is idempotent and cannot accumulate duplicates.
            geofencingClient.removeGeofences(geofencePendingIntent).await()
            geofencingClient.addGeofences(request, geofencePendingIntent).await()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: SecurityException) {
            throw permissionDenied(exception)
        } catch (exception: ApiException) {
            throw GeofenceRegistrationException(
                message = exception.message ?: "Falha ao atualizar o monitoramento de localização.",
                retryable = isRetryable(exception.statusCode),
                cause = exception
            )
        }
    }

    override suspend fun removeGeofences() {
        try {
            geofencingClient.removeGeofences(geofencePendingIntent).await()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: SecurityException) {
            throw permissionDenied(exception)
        } catch (exception: ApiException) {
            throw GeofenceRegistrationException(
                message = exception.message ?: "Falha ao remover o monitoramento de localização.",
                retryable = isRetryable(exception.statusCode),
                cause = exception
            )
        }
    }

    private companion object {
        const val LOITERING_DELAY_MS = 30_000L
        const val CHECK_IN_TRANSITIONS =
            Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL
        const val INITIAL_TRIGGERS =
            GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_DWELL
        val RETRYABLE_STATUS_CODES = setOf(
            CommonStatusCodes.NETWORK_ERROR,
            CommonStatusCodes.INTERNAL_ERROR,
            CommonStatusCodes.API_NOT_CONNECTED,
            CommonStatusCodes.TIMEOUT,
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE
        )

        fun isRetryable(statusCode: Int): Boolean = statusCode in RETRYABLE_STATUS_CODES

        fun permissionDenied(exception: SecurityException) = GeofenceRegistrationException(
            message = exception.message ?: "Permissão de localização insuficiente.",
            retryable = false,
            cause = exception
        )
    }
}
