package com.presencial.app.di

import com.google.firebase.crashlytics.FirebaseCrashlytics
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
        noOpCrashReporter: NoOpCrashReporter
    ): CrashReporter = if (BuildConfig.CRASHLYTICS_ENABLED) {
        FirebaseCrashReporter(FirebaseCrashlytics.getInstance())
    } else {
        noOpCrashReporter
    }
}
