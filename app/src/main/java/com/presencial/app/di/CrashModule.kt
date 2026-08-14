package com.presencial.app.di

import com.presencial.app.data.crash.CrashlyticsAccess
import com.presencial.app.data.crash.FirebaseCrashReporter
import com.presencial.app.data.crash.NoOpCrashReporter
import com.presencial.app.domain.util.CrashReporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CrashModule {

    @Provides
    @Singleton
    fun provideCrashReporter(
        noOpCrashReporter: NoOpCrashReporter
    ): CrashReporter {
        val crashlytics = CrashlyticsAccess.getOrNull() ?: return noOpCrashReporter
        return FirebaseCrashReporter(crashlytics)
    }
}
