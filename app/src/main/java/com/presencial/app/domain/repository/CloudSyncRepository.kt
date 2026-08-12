package com.presencial.app.domain.repository

import android.net.Uri
import com.presencial.app.domain.model.CloudStorageProvider
import com.presencial.app.domain.model.CloudSyncState
import kotlinx.coroutines.flow.StateFlow

interface CloudSyncRepository {
    val syncState: StateFlow<CloudSyncState>

    suspend fun connectFolder(treeUri: Uri, provider: CloudStorageProvider): Result<Unit>

    suspend fun setSelectedProvider(provider: CloudStorageProvider)

    suspend fun signOut()

    suspend fun uploadBackup(): Result<Unit>

    suspend fun restoreBackup(): Result<Unit>

    suspend fun refreshState()
}
