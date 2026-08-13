package com.presencial.app.data.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FirebaseCrashReporterTest {

    private val crashlytics = mockk<FirebaseCrashlytics>(relaxed = true)
    private lateinit var reporter: FirebaseCrashReporter

    @BeforeEach
    fun setup() {
        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns crashlytics
        reporter = FirebaseCrashReporter()
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(FirebaseCrashlytics::class)
    }

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
