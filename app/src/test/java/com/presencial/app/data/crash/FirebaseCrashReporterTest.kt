package com.presencial.app.data.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class FirebaseCrashReporterTest {

    private val crashlytics = mockk<FirebaseCrashlytics>(relaxed = true)
    private val reporter = FirebaseCrashReporter(crashlytics)

    @Test
    fun `recordNonFatal should delegate to FirebaseCrashlytics`() {
        val error = IllegalStateException("test")

        reporter.recordNonFatal(error)

        verify { crashlytics.recordException(error) }
    }

    @Test
    fun `log should delegate to FirebaseCrashlytics`() {
        reporter.log("check-in failed")

        verify { crashlytics.log("check-in failed") }
    }
}
