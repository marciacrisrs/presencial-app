package com.presencial.app

import android.os.Looper
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import com.presencial.app.notification.NotificationScheduler
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PresencialAppTest {

    @BeforeEach
    fun setup() {
        mockkStatic(Looper::class)
        mockkStatic(Log::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>(), any()) } returns 0
        every { Log.w(any(), any<Throwable>()) } returns 0
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Looper::class)
        unmockkStatic(Log::class)
    }

    @Test
    fun `workManagerConfiguration returns configuration with worker factory`() {
        val app = PresencialApp()
        val mockWorkerFactory = mockk<HiltWorkerFactory>()
        val mockNotificationScheduler = mockk<NotificationScheduler>()
        
        app.workerFactory = mockWorkerFactory
        app.notificationScheduler = mockNotificationScheduler
        
        val config = app.workManagerConfiguration
        assertNotNull(config)
    }
}
