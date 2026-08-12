package com.presencial.app.domain.location

import com.presencial.app.domain.usecase.SyncGeofencesUseCase
import com.presencial.app.domain.widget.WidgetRefresher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BootCompletedHandler @Inject constructor(
    private val syncGeofencesUseCase: SyncGeofencesUseCase,
    private val widgetRefresher: WidgetRefresher
) {
    suspend fun handleBootCompleted() {
        syncGeofencesUseCase()
        widgetRefresher.refresh()
    }
}
