package com.presencial.app.data.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.presencial.app.domain.util.CrashReporter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCrashReporter @Inject constructor() : CrashReporter {
    override fun recordNonFatal(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    override fun log(message: String) {
        FirebaseCrashlytics.getInstance().log(message)
    }
}
