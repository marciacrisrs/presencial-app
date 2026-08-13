package com.presencial.app.data.crash

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class NoOpCrashReporterTest {

    private val reporter = NoOpCrashReporter()

    @Test
    fun `recordNonFatal should not throw`() {
        assertDoesNotThrow {
            reporter.recordNonFatal(IllegalStateException("test"))
        }
    }

    @Test
    fun `log should not throw`() {
        assertDoesNotThrow {
            reporter.log("test message")
        }
    }
}
