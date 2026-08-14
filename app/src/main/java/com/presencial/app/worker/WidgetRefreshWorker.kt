package com.presencial.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.presencial.app.domain.widget.WidgetRefresher
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WidgetRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val widgetRefresher: WidgetRefresher
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        widgetRefresher.refresh()
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "widget_refresh"
    }
}
