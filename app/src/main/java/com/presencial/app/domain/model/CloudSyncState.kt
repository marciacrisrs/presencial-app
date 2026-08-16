package com.presencial.app.domain.model

data class CloudSyncState(
    val provider: CloudStorageProvider = CloudStorageProvider.GOOGLE_DRIVE,
    val isSignedIn: Boolean = false,
    val folderStatus: BackupFolderStatus = BackupFolderStatus.NOT_CHOSEN,
    val backupExists: Boolean = false,
    val accountEmail: String? = null,
    val lastSyncEpochMillis: Long? = null,
    val isSyncing: Boolean = false
)
