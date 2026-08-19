package com.presencial.app.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.presencial.app.worker.GeofenceRestoreWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceRestoreScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleRestore(policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP) {
        val workRequest = OneTimeWorkRequestBuilder<GeofenceRestoreWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            GeofenceRestoreWorker.WORK_NAME,
            policy,
            workRequest
        )
    }
}
