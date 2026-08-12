package com.presencial.app.di

import com.presencial.app.BuildConfig
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
        firebaseCrashReporter: FirebaseCrashReporter,
        noOpCrashReporter: NoOpCrashReporter
    ): CrashReporter = if (BuildConfig.CRASHLYTICS_ENABLED) {
        firebaseCrashReporter
    } else {
        noOpCrashReporter
    }
}
