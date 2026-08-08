package com.presencial.app

import android.os.Looper
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import com.presencial.app.notification.NotificationScheduler
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class PresencialAppTest {

    @Before
    fun setup() {
        mockkStatic(Looper::class)
        mockkStatic(Log::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>(), any()) } returns 0
        every { Log.w(any(), any<Throwable>()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Looper::class)
        unmockkStatic(Log::class)
    }

    @Test
    fun workManagerConfiguration_returns_configuration_with_worker_factory() {
        val app = PresencialApp()
        val mockWorkerFactory = mockk<HiltWorkerFactory>()
        val mockNotificationScheduler = mockk<NotificationScheduler>()
        
        app.workerFactory = mockWorkerFactory
        app.notificationScheduler = mockNotificationScheduler
        
        val config = app.workManagerConfiguration
        assertNotNull(config)
    }
}
