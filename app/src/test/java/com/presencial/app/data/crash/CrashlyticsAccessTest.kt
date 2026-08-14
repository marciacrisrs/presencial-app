package com.presencial.app.data.crash

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CrashlyticsAccessTest {

    private val context = mockk<Context>(relaxed = true)

    @BeforeEach
    fun setup() {
        mockkStatic(FirebaseCrashlytics::class)
        mockkStatic(FirebaseApp::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(FirebaseCrashlytics::class)
        unmockkStatic(FirebaseApp::class)
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

    @Test
    fun `ensureInitialized initializes Firebase when no app exists`() {
        every { FirebaseApp.getApps(context) } returns emptyList()
        every { FirebaseApp.initializeApp(context) } returns mockk()

        CrashlyticsAccess.ensureInitialized(context)

        verify(exactly = 1) { FirebaseApp.initializeApp(context) }
    }

    @Test
    fun `ensureInitialized skips initialize when app already exists`() {
        every { FirebaseApp.getApps(context) } returns listOf(mockk())

        CrashlyticsAccess.ensureInitialized(context)

        verify(exactly = 0) { FirebaseApp.initializeApp(context) }
    }

    @Test
    fun `ensureInitialized does not throw when Firebase fails`() {
        every { FirebaseApp.getApps(context) } throws IllegalStateException("pairip")

        CrashlyticsAccess.ensureInitialized(context)
    }
}
