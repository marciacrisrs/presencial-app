package com.presencial.app.data.sync

import com.presencial.app.data.backup.BackupManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CloudSyncRepositoryImplTest {

    private val folderSyncProvider = mockk<CloudFolderSyncProvider>()
    private val backupManager = mockk<BackupManager>()
    private val preferences = mockk<CloudSyncPreferences>()
    private lateinit var repository: CloudSyncRepositoryImpl

    @BeforeEach
    fun setup() {
        coEvery { folderSyncProvider.isSignedIn() } returns true
        coEvery { folderSyncProvider.getAccountEmail() } returns "Presencial Backup"
        coEvery { folderSyncProvider.selectedProvider() } returns
            com.presencial.app.domain.model.CloudStorageProvider.GOOGLE_DRIVE
        coEvery { preferences.getLastSyncEpochMillis() } returns null
        coEvery { folderSyncProvider.isFolderAccessible() } returns true
        coEvery { folderSyncProvider.backupExists() } returns true
        repository = CloudSyncRepositoryImpl(folderSyncProvider, backupManager, preferences)
    }

    @Test
    fun `uploadBackup exports bytes and uploads to folder`() = runTest {
        val payload = """{"version":3}""".toByteArray()
        coEvery { backupManager.exportToBytes() } returns Result.success(payload)
        coEvery {
            folderSyncProvider.uploadBackup(CloudSyncRepositoryImpl.BACKUP_FILE_NAME, payload)
        } returns Result.success(Unit)
        coEvery { preferences.setLastSyncEpochMillis(any()) } returns Unit

        val result = repository.uploadBackup()

        assertTrue(result.isSuccess)
        coVerify { folderSyncProvider.uploadBackup(CloudSyncRepositoryImpl.BACKUP_FILE_NAME, payload) }
        coVerify { preferences.setLastSyncEpochMillis(any()) }
    }

    @Test
    fun `restoreBackup downloads bytes and imports backup`() = runTest {
        val payload = """{"version":3}""".toByteArray()
        coEvery {
            folderSyncProvider.downloadBackup(CloudSyncRepositoryImpl.BACKUP_FILE_NAME)
        } returns Result.success(payload)
        coEvery { backupManager.importFromBytes(payload) } returns Result.success(Unit)

        val result = repository.restoreBackup()

        assertTrue(result.isSuccess)
        coVerify { backupManager.importFromBytes(payload) }
    }

    @Test
    fun `uploadBackup fails when folder not connected`() = runTest {
        coEvery { folderSyncProvider.isSignedIn() } returns false
        coEvery { folderSyncProvider.isFolderAccessible() } returns false

        val result = repository.uploadBackup()

        assertTrue(result.isFailure)
        assertEquals("Escolha uma pasta de backup primeiro", result.exceptionOrNull()?.message)
    }

    @Test
    fun `uploadBackup keeps last sync after refresh when folder remains accessible`() = runTest {
        val payload = """{"version":3}""".toByteArray()
        val syncTime = 1_700_000_000_000L
        coEvery { backupManager.exportToBytes() } returns Result.success(payload)
        coEvery {
            folderSyncProvider.uploadBackup(CloudSyncRepositoryImpl.BACKUP_FILE_NAME, payload)
        } returns Result.success(Unit)
        coEvery { preferences.setLastSyncEpochMillis(any()) } coAnswers {
            coEvery { preferences.getLastSyncEpochMillis() } returns syncTime
        }
        coEvery { folderSyncProvider.isFolderAccessible() } returns true

        val result = repository.uploadBackup()

        assertTrue(result.isSuccess)
        assertEquals(syncTime, repository.syncState.value.lastSyncEpochMillis)
        assertTrue(repository.syncState.value.isSignedIn)
        coVerify(exactly = 0) { folderSyncProvider.signOut() }
        coVerify(exactly = 0) { preferences.clearLastSyncEpochMillis() }
    }

    @Test
    fun `refreshState keeps last sync when folder permission is revoked`() = runTest {
        val syncTime = 1_700_000_000_000L
        coEvery { folderSyncProvider.isSignedIn() } returns true
        coEvery { folderSyncProvider.isFolderAccessible() } returns false
        coEvery { folderSyncProvider.backupExists() } returns false
        coEvery { preferences.getLastSyncEpochMillis() } returns syncTime
        coEvery { folderSyncProvider.getAccountEmail() } returns "Presencial Backup"

        repository.refreshState()

        coVerify(exactly = 0) { folderSyncProvider.signOut() }
        coVerify(exactly = 0) { preferences.clearLastSyncEpochMillis() }
        assertEquals(false, repository.syncState.value.isSignedIn)
        assertEquals(
            com.presencial.app.domain.model.BackupFolderStatus.PERMISSION_REVOKED,
            repository.syncState.value.folderStatus
        )
        assertEquals(syncTime, repository.syncState.value.lastSyncEpochMillis)
        assertEquals("Presencial Backup", repository.syncState.value.accountEmail)
    }

    @Test
    fun `restoreBackup fails when folder not connected`() = runTest {
        coEvery { folderSyncProvider.isSignedIn() } returns false
        coEvery { folderSyncProvider.isFolderAccessible() } returns false

        val result = repository.restoreBackup()

        assertTrue(result.isFailure)
        assertEquals("Escolha uma pasta de backup primeiro", result.exceptionOrNull()?.message)
    }

    @Test
    fun `signOut clears preferences and refreshes state`() = runTest {
        coEvery { folderSyncProvider.signOut() } returns Unit
        coEvery { preferences.clearLastSyncEpochMillis() } returns Unit
        coEvery { folderSyncProvider.isSignedIn() } returns false
        coEvery { folderSyncProvider.getAccountEmail() } returns null

        repository.signOut()

        coVerify { folderSyncProvider.signOut() }
        coVerify { preferences.clearLastSyncEpochMillis() }
    }

    @Test
    fun `setSelectedProvider delegates to folder provider`() = runTest {
        val provider = com.presencial.app.domain.model.CloudStorageProvider.ONEDRIVE
        coEvery { folderSyncProvider.setSelectedProvider(provider) } returns Unit

        repository.setSelectedProvider(provider)

        coVerify { folderSyncProvider.setSelectedProvider(provider) }
    }

    @Test
    fun `connectFolder delegates to folder provider and refreshes state`() = runTest {
        val uri = mockk<android.net.Uri>()
        val provider = com.presencial.app.domain.model.CloudStorageProvider.DROPBOX
        coEvery { folderSyncProvider.connectFolder(uri, provider) } returns Unit

        val result = repository.connectFolder(uri, provider)

        assertTrue(result.isSuccess)
        coVerify { folderSyncProvider.connectFolder(uri, provider) }
    }
}
