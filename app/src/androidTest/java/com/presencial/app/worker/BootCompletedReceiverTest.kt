package com.presencial.app.worker

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BootCompletedReceiverTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun onReceive_withWrongAction_doesNotCrash() {
        val receiver = BootCompletedReceiver()
        receiver.onReceive(context, Intent(Intent.ACTION_VIEW))
    }

    @Test
    fun onReceive_withBootCompleted_doesNotCrash() {
        val receiver = BootCompletedReceiver()
        receiver.onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))
    }

    @Test
    fun onReceive_withLockedBootCompleted_doesNotCrash() {
        val receiver = BootCompletedReceiver()
        receiver.onReceive(context, Intent(Intent.ACTION_LOCKED_BOOT_COMPLETED))
    }

    @Test
    fun onReceive_withPackageReplaced_doesNotCrash() {
        val receiver = BootCompletedReceiver()
        receiver.onReceive(context, Intent(Intent.ACTION_MY_PACKAGE_REPLACED))
    }

    @Test
    fun onReceive_withQuickBootPowerOn_doesNotCrash() {
        val receiver = BootCompletedReceiver()
        receiver.onReceive(
            context,
            Intent(GeofenceRestoreEvents.ACTION_QUICKBOOT_POWERON)
        )
    }
}
