package com.presencial.app.domain.location

import android.app.PendingIntent
import android.content.Context
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.presencial.app.domain.model.WorkAddress
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GeofenceManagerTest {

    private val context: Context = mockk(relaxed = true)
    private val geofencingClient: GeofencingClient = mockk(relaxed = true)
    private lateinit var geofenceManager: GeofenceManager

    @BeforeEach
    fun setup() {
        mockkStatic(LocationServices::class)
        every { LocationServices.getGeofencingClient(any<Context>()) } returns geofencingClient
        geofenceManager = GeofenceManager(context)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(LocationServices::class)
    }

    @Test
    fun `registerGeofences with empty list calls removeGeofences`() {
        geofenceManager.registerGeofences(emptyList())
        verify { geofencingClient.removeGeofences(any<PendingIntent>()) }
    }

    @Test
    fun `registerGeofences with active addresses calls addGeofences`() {
        val addresses = listOf(
            WorkAddress(id = 1, name = "Home", addressText = "Street 1", latitude = 1.0, longitude = 1.0, isActive = true),
            WorkAddress(id = 2, name = "Office", addressText = "Street 2", latitude = 2.0, longitude = 2.0, isActive = true)
        )
        
        val mockTask: Task<Void> = mockk(relaxed = true)
        every { geofencingClient.addGeofences(any(), any()) } returns mockTask

        geofenceManager.registerGeofences(addresses)

        verify {
            geofencingClient.addGeofences(
                match { request ->
                    request is GeofencingRequest && request.geofences.size == 2
                },
                any()
            )
        }
    }

    @Test
    fun `removeGeofences calls client remove`() {
        geofenceManager.removeGeofences()
        verify { geofencingClient.removeGeofences(any<PendingIntent>()) }
    }
}
