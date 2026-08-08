package com.presencial.app

import androidx.hilt.work.HiltWorkerFactory
import com.presencial.app.notification.NotificationScheduler
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class PresencialAppTest {

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
