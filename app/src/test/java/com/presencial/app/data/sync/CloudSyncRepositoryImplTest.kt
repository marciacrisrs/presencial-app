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

        val result = repository.uploadBackup()

        assertTrue(result.isFailure)
        assertEquals("Selecione uma pasta na nuvem primeiro", result.exceptionOrNull()?.message)
    }
}
