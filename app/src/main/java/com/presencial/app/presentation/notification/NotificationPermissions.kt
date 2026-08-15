package com.presencial.app.presentation.notification

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestNotificationPermissionOnLaunch(enabled: Boolean = true) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    LaunchedEffect(enabled, permissionState.status) {
        if (!enabled) return@LaunchedEffect
        if (!permissionState.status.isGranted && !permissionState.status.shouldShowRationale) {
            permissionState.launchPermissionRequest()
        }
    }
}
