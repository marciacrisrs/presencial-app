package com.presencial.app

import androidx.hilt.work.HiltWorkerFactory
import com.presencial.app.notification.NotificationScheduler
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class PresencialAppTest {

    @Before
    fun setup() {
        // Só mantenha os mocks de Android se forem realmente necessários
    }

    @After
    fun tearDown() {
        // Limpeza dos mocks, caso existam
    }

    @Test
    fun workManagerConfiguration_returns_configuration_with_worker_factory() {
        val app = PresencialApp()

        app.workerFactory = mockk<HiltWorkerFactory>()
        app.notificationScheduler = mockk<NotificationScheduler>()

        val config = app.workManagerConfiguration

        assertNotNull(config)
    }
}
