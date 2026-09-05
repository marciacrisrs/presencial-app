package com.presencial.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.presencial.app.data.crash.CrashlyticsAccess
import com.presencial.app.domain.holidays.HolidayScopeManager
import com.presencial.app.domain.util.CrashReporter
import com.presencial.app.domain.usecase.ResolveWorkAddressLocationUseCase
import com.presencial.app.domain.usecase.SyncGeofencesUseCase
import com.presencial.app.domain.widget.WidgetRefresher
import com.presencial.app.notification.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import javax.inject.Inject

@HiltAndroidApp
class PresencialApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notificationScheduler: NotificationScheduler
    @Inject lateinit var syncGeofencesUseCase: SyncGeofencesUseCase
    @Inject lateinit var widgetRefresher: WidgetRefresher
    @Inject lateinit var holidayScopeManager: HolidayScopeManager
    @Inject lateinit var resolveWorkAddressLocationUseCase: ResolveWorkAddressLocationUseCase
    @Inject lateinit var crashReporter: CrashReporter

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        CrashlyticsAccess.ensureInitialized(this)
        super.onCreate()
        CrashlyticsAccess.getOrNull()?.isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
        crashReporter.log("PresencialApp started")
        holidayScopeManager
        notificationScheduler.scheduleDailyReminder()
        appScope.launch {
            resolveWorkAddressLocationUseCase.backfillMissingLocations()
            try {
                syncGeofencesUseCase()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IllegalStateException) {
                crashReporter.recordNonFatal(exception)
            }
            widgetRefresher.refresh()
        }
    }
}
