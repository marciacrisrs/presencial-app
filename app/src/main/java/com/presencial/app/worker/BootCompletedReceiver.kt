package com.presencial.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.presencial.app.domain.usecase.SyncGeofencesUseCase
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
        fun syncGeofencesUseCase(): SyncGeofencesUseCase
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            BootEntryPoint::class.java
        )

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                entryPoint.syncGeofencesUseCase()()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
