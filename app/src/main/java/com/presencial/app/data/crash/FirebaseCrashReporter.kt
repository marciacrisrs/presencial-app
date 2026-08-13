package com.presencial.app.data.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.presencial.app.domain.util.CrashReporter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCrashReporter @Inject constructor(
    private val crashlytics: FirebaseCrashlytics
) : CrashReporter {
    override fun recordNonFatal(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }
}
