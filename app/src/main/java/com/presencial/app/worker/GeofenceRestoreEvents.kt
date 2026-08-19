package com.presencial.app.worker

import android.content.Intent

object GeofenceRestoreEvents {
    const val ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"

    val SUPPORTED_ACTIONS: Set<String> = setOf(
        Intent.ACTION_BOOT_COMPLETED,
        Intent.ACTION_LOCKED_BOOT_COMPLETED,
        Intent.ACTION_MY_PACKAGE_REPLACED,
        ACTION_QUICKBOOT_POWERON,
    )

    fun isSupported(action: String?): Boolean = action in SUPPORTED_ACTIONS
}
