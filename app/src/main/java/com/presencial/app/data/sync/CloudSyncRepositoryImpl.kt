package com.presencial.app.data.sync

import android.net.Uri
import com.presencial.app.data.backup.BackupManager
import com.presencial.app.domain.model.BackupFolderStatus
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
        if (!folderSyncProvider.isFolderAccessible()) {
            return Result.failure(IllegalStateException("Escolha uma pasta de backup primeiro"))
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
        if (!folderSyncProvider.isFolderAccessible()) {
            return Result.failure(IllegalStateException("Escolha uma pasta de backup primeiro"))
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
        val hasFolder = folderSyncProvider.isSignedIn()
        val accessible = hasFolder && folderSyncProvider.isFolderAccessible()
        val backupExists = accessible && folderSyncProvider.backupExists()
        val folderStatus = when {
            !hasFolder -> BackupFolderStatus.NOT_CHOSEN
            !accessible -> BackupFolderStatus.PERMISSION_REVOKED
            else -> BackupFolderStatus.ACCESSIBLE
        }
        _syncState.update {
            CloudSyncState(
                provider = folderSyncProvider.selectedProvider(),
                isSignedIn = accessible,
                folderStatus = folderStatus,
                backupExists = backupExists,
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
