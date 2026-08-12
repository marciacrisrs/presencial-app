package com.presencial.app.worker

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeofenceBroadcastReceiverTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun onReceive_withEmptyIntent_doesNotCrash() {
        val receiver = GeofenceBroadcastReceiver()
        receiver.onReceive(context, Intent())
    }

    @Test
    fun onReceive_withNullGeofencingEvent_doesNotCrash() {
        val receiver = GeofenceBroadcastReceiver()
        receiver.onReceive(context, Intent("com.google.android.gms.location.Geofence"))
    }
}
