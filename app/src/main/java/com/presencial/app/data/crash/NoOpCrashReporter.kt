package com.presencial.app.data.crash

import com.presencial.app.domain.util.CrashReporter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpCrashReporter @Inject constructor() : CrashReporter {
    override fun recordNonFatal(throwable: Throwable) = Unit
    override fun log(message: String) = Unit
}
