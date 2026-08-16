package com.presencial.app.domain.location

import android.app.PendingIntent
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.presencial.app.data.location.AndroidGeofenceRegistrar
import com.presencial.app.domain.model.WorkAddress
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeofenceManagerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val geofencingClient: GeofencingClient = mockk(relaxed = true)
    private lateinit var geofenceRegistrar: AndroidGeofenceRegistrar

    @Before
    fun setup() {
        mockkStatic(LocationServices::class)
        every { LocationServices.getGeofencingClient(any<Context>()) } returns geofencingClient
        geofenceRegistrar = AndroidGeofenceRegistrar(context)
    }

    @After
    fun tearDown() {
        unmockkStatic(LocationServices::class)
    }

    @Test
    fun registerGeofences_withEmptyList_callsRemoveGeofences() = runBlocking {
        val mockTask: Task<Void> = mockk()
        every { mockTask.isComplete } returns true
        every { mockTask.isSuccessful } returns true
        every { mockTask.exception } returns null
        every { geofencingClient.removeGeofences(any<PendingIntent>()) } returns mockTask

        geofenceRegistrar.registerGeofences(emptyList())

        verify { geofencingClient.removeGeofences(any<PendingIntent>()) }
    }

    @Test
    fun registerGeofences_withActiveAddresses_callsAddGeofences() = runBlocking {
        val addresses = listOf(
            WorkAddress(
                id = 1, name = "Home", addressText = "Street 1",
                latitude = 1.0, longitude = 1.0, isActive = true
            )
        )

        val mockTask: Task<Void> = mockk()
        every { mockTask.isComplete } returns true
        every { mockTask.isSuccessful } returns true
        every { mockTask.exception } returns null
        every { geofencingClient.addGeofences(any(), any()) } returns mockTask

        geofenceRegistrar.registerGeofences(addresses)

        verify {
            geofencingClient.addGeofences(
                match { it is GeofencingRequest && it.geofences.size == 1 },
                any()
            )
        }
    }
}
