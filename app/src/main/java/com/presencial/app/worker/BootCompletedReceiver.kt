package com.presencial.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.presencial.app.domain.location.BootCompletedHandler
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

class BootCompletedReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootEntryPoint {
        fun bootCompletedHandler(): BootCompletedHandler
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (!GeofenceRestoreEvents.isSupported(intent.action)) return

        val handler = EntryPointAccessors.fromApplication(
            context.applicationContext,
            BootEntryPoint::class.java
        ).bootCompletedHandler()

        handler.handleSystemRestore()
    }
}
