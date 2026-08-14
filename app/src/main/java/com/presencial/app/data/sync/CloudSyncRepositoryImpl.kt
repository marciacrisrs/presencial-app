package com.presencial.app.data.sync

import android.net.Uri
import com.presencial.app.data.backup.BackupManager
import com.presencial.app.domain.model.CloudStorageProvider
import com.presencial.app.domain.model.CloudSyncState
import com.presencial.app.domain.repository.CloudSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudSyncRepositoryImpl @Inject constructor(
    private val folderSyncProvider: CloudFolderSyncProvider,
    private val backupManager: BackupManager,
    private val cloudSyncPreferences: CloudSyncPreferences
) : CloudSyncRepository {

    private val _syncState = MutableStateFlow(CloudSyncState())
    override val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

    override suspend fun connectFolder(treeUri: Uri, provider: CloudStorageProvider): Result<Unit> =
        runCatching {
            folderSyncProvider.connectFolder(treeUri, provider)
            refreshState()
        }

    override suspend fun setSelectedProvider(provider: CloudStorageProvider) {
        folderSyncProvider.setSelectedProvider(provider)
        refreshState()
    }

    override suspend fun signOut() {
        folderSyncProvider.signOut()
        cloudSyncPreferences.clearLastSyncEpochMillis()
        refreshState()
    }

    override suspend fun uploadBackup(): Result<Unit> {
        if (!folderSyncProvider.isSignedIn()) {
            return Result.failure(IllegalStateException("Selecione uma pasta na nuvem primeiro"))
        }
        _syncState.update { it.copy(isSyncing = true) }
        val result = runCatching {
            val bytes = backupManager.exportToBytes().getOrThrow()
            folderSyncProvider.uploadBackup(BACKUP_FILE_NAME, bytes).getOrThrow()
            cloudSyncPreferences.setLastSyncEpochMillis(System.currentTimeMillis())
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) }
        )
        refreshState()
        _syncState.update { it.copy(isSyncing = false) }
        return result
    }

    override suspend fun restoreBackup(): Result<Unit> {
        if (!folderSyncProvider.isSignedIn()) {
            return Result.failure(IllegalStateException("Selecione uma pasta na nuvem primeiro"))
        }
        _syncState.update { it.copy(isSyncing = true) }
        val result = runCatching {
            val bytes = folderSyncProvider.downloadBackup(BACKUP_FILE_NAME).getOrThrow()
            backupManager.importFromBytes(bytes).getOrThrow()
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) }
        )
        refreshState()
        _syncState.update { it.copy(isSyncing = false) }
        return result
    }

    override suspend fun refreshState() {
        if (folderSyncProvider.isSignedIn() && !folderSyncProvider.isFolderAccessible()) {
            folderSyncProvider.signOut()
            cloudSyncPreferences.clearLastSyncEpochMillis()
        }
        _syncState.update {
            CloudSyncState(
                provider = folderSyncProvider.selectedProvider(),
                isSignedIn = folderSyncProvider.isSignedIn(),
                accountEmail = folderSyncProvider.getAccountEmail(),
                lastSyncEpochMillis = cloudSyncPreferences.getLastSyncEpochMillis(),
                isSyncing = it.isSyncing
            )
        }
    }

    companion object {
        const val BACKUP_FILE_NAME = CloudFolderSyncProvider.BACKUP_FILE_NAME
    }
}
