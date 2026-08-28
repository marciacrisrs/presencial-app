package com.presencial.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.presencial.app.domain.location.GeofenceRegistrationException
import com.presencial.app.domain.usecase.SyncGeofencesUseCase
import com.presencial.app.domain.widget.WidgetRefresher
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.CancellationException

@HiltWorker
class GeofenceRestoreWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncGeofencesUseCase: SyncGeofencesUseCase,
    private val widgetRefresher: WidgetRefresher
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            syncGeofencesUseCase()
            widgetRefresher.refresh()
            Result.success()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: GeofenceRegistrationException) {
            restoreFailure(exception, retryable = exception.retryable)
        } catch (exception: IllegalStateException) {
            restoreFailure(exception, retryable = true)
        }
    }

    private fun restoreFailure(exception: IllegalStateException, retryable: Boolean): Result {
        Log.w(TAG, "Geofence restore failed on attempt ${runAttemptCount + 1}", exception)
        return if (retryable && GeofenceRestoreEvents.shouldRetryRestore(runAttemptCount)) {
            Result.retry()
        } else {
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "geofence_restore"
        private const val TAG = "GeofenceRestoreWorker"
    }
}
