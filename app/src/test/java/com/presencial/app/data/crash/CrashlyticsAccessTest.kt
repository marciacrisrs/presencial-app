package com.presencial.app.data.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CrashlyticsAccessTest {

    @BeforeEach
    fun setup() {
        mockkStatic(FirebaseCrashlytics::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(FirebaseCrashlytics::class)
    }

    @Test
    fun `getOrNull returns instance when Firebase is initialized`() {
        every { FirebaseCrashlytics.getInstance() } returns mockk()

        assertNotNull(CrashlyticsAccess.getOrNull())
    }

    @Test
    fun `getOrNull returns null when Firebase is missing`() {
        every { FirebaseCrashlytics.getInstance() } throws IllegalStateException("not initialized")

        assertNull(CrashlyticsAccess.getOrNull())
    }
}
