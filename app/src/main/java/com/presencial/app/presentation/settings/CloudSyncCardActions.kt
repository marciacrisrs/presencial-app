package com.presencial.app.presentation.settings

data class CloudSyncCardActions(
    val onConnectFolder: () -> Unit,
    val onSignOut: () -> Unit,
    val onUpload: () -> Unit,
    val onRestore: () -> Unit,
    val onExportFile: () -> Unit,
    val onRestoreFile: () -> Unit
)
