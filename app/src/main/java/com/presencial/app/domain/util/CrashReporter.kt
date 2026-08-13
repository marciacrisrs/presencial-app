package com.presencial.app.domain.util

interface CrashReporter {
    fun recordNonFatal(throwable: Throwable)
    fun log(message: String)
}
