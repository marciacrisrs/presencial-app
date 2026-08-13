package com.presencial.app.data.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.presencial.app.domain.util.CrashReporter

class FirebaseCrashReporter(
    private val crashlytics: FirebaseCrashlytics
) : CrashReporter {
    override fun recordNonFatal(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }
}
