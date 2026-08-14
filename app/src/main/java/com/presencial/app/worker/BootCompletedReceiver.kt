package com.presencial.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.presencial.app.domain.location.BootCompletedHandler
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootEntryPoint {
        fun bootCompletedHandler(): BootCompletedHandler
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        val handler = EntryPointAccessors.fromApplication(
            context.applicationContext,
            BootEntryPoint::class.java
        ).bootCompletedHandler()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                handler.handleBootCompleted()
            } finally {
                pendingResult?.finish()
            }
        }
    }
}
