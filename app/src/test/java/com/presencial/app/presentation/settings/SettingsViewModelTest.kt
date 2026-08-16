package com.presencial.app.presentation.settings

import app.cash.turbine.test
import com.presencial.app.data.backup.BackupManager
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.CloudStorageProvider
import com.presencial.app.domain.model.CloudSyncState
import com.presencial.app.domain.model.GeofenceSyncStatus
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.repository.CloudSyncRepository
import com.presencial.app.domain.repository.GeofenceSyncStatusRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.repository.WorkAddressRepository
import com.presencial.app.domain.usecase.SyncGeofencesUseCase
import com.presencial.app.domain.widget.WidgetRefresher
import com.presencial.app.util.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.io.File
import java.io.OutputStream

class SettingsViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val settingsRepository = mockk<SettingsRepository>()
    private val backupManager = mockk<BackupManager>()
    private val cloudSyncRepository = mockk<CloudSyncRepository>()
    private val syncGeofencesUseCase = mockk<SyncGeofencesUseCase>()
    private val geofenceSyncStatusRepository = mockk<GeofenceSyncStatusRepository>()
    private val workAddressRepository = mockk<WorkAddressRepository>()
    private val widgetRefresher = mockk<WidgetRefresher>()
    private lateinit var viewModel: SettingsViewModel

    @BeforeEach
    fun setup() {
        every { settingsRepository.settings } returns flowOf(AppSettings())
        every { workAddressRepository.getAllAddresses() } returns flowOf(emptyList())
        every { cloudSyncRepository.syncState } returns MutableStateFlow(CloudSyncState())
        every { geofenceSyncStatusRepository.status } returns flowOf(GeofenceSyncStatus.Unknown)
        coEvery { cloudSyncRepository.refreshState() } returns Unit
        coEvery { syncGeofencesUseCase() } returns Unit
        coEvery { widgetRefresher.refresh() } returns Unit
        viewModel = createViewModel()
    }

    private fun createViewModel() = SettingsViewModel(
        settingsRepository,
        backupManager,
        cloudSyncRepository,
        syncGeofencesUseCase,
        geofenceSyncStatusRepository,
        workAddressRepository,
        widgetRefresher
    )

    @Test
    fun `settings should reflect repository flow`() = runTest {
        val appSettings = AppSettings(requiredPercentage = 50, countSaturdaysAsWorkdays = true)
        every { settingsRepository.settings } returns flowOf(appSettings)

        viewModel = createViewModel()

        viewModel.settings.test {
            assertEquals(appSettings, awaitItem())
        }
    }

    @Test
    fun `geofence sync status should reflect repository flow`() = runTest {
        every { geofenceSyncStatusRepository.status } returns flowOf(
            GeofenceSyncStatus.Failure("permission denied")
        )

        viewModel = createViewModel()

        viewModel.geofenceSyncStatus.test {
            assertEquals(GeofenceSyncStatus.Failure("permission denied"), awaitItem())
        }
    }

    @Test
    fun `retryGeofenceSync should call sync use case`() = runTest {
        viewModel.retryGeofenceSync()

        coVerify { syncGeofencesUseCase() }
        assertEquals("Monitoramento atualizado com sucesso.", viewModel.message.value)
    }

    @Test
    fun `updatePresencePolicy should call repository`() = runTest {
        val policy = PresencePolicy(companyName = "Acme", freePercentage = 50)
        coEvery { settingsRepository.updatePresencePolicy(policy) } returns Unit

        viewModel.updatePresencePolicy(policy)

        coVerify { settingsRepository.updatePresencePolicy(policy) }
    }

    @Test
    fun `updateSaturdays should call repository`() = runTest {
        coEvery { settingsRepository.updateCountSaturdaysAsWorkdays(true) } returns Unit

        viewModel.updateSaturdays(true)

        coVerify { settingsRepository.updateCountSaturdaysAsWorkdays(true) }
    }

    @Test
    fun `exportBackup should show success message on success`() = runTest {
        val outputStream = mockk<OutputStream>()
        coEvery { backupManager.exportToStream(outputStream) } returns Result.success(Unit)

        viewModel.exportBackup(outputStream)

        assertEquals("Backup exportado com sucesso!", viewModel.message.value)
    }

    @Test
    fun `exportBackup should show error message on failure`() = runTest {
        val outputStream = mockk<OutputStream>()
        coEvery { backupManager.exportToStream(outputStream) } returns Result.failure(Exception("Disk full"))

        viewModel.exportBackup(outputStream)

        assertEquals("Erro ao exportar: Disk full", viewModel.message.value)
    }

    @Test
    fun `exportBackup should do nothing if outputStream is null`() = runTest {
        viewModel.exportBackup(null)
        coVerify(exactly = 0) { backupManager.exportToStream(any()) }
        assertNull(viewModel.message.value)
    }

    @Test
    fun `importBackup should sync geofences on success`() = runTest {
        val file = mockk<File>()
        coEvery { backupManager.importFromFile(file) } returns Result.success(Unit)

        viewModel.importBackup(file)

        coVerify { syncGeofencesUseCase() }
        coVerify { widgetRefresher.refresh() }
        assertEquals("Backup restaurado com sucesso!", viewModel.message.value)
    }

    @Test
    fun `importBackup should show error message on failure`() = runTest {
        val file = mockk<File>()
        coEvery { backupManager.importFromFile(file) } returns Result.failure(Exception("Invalid file"))

        viewModel.importBackup(file)

        coVerify(exactly = 0) { syncGeofencesUseCase() }
        assertEquals("Erro ao restaurar: Invalid file", viewModel.message.value)
    }

    @Test
    fun `uploadCloudBackup should show success message`() = runTest {
        coEvery { cloudSyncRepository.uploadBackup() } returns Result.success(Unit)

        viewModel.uploadCloudBackup()

        assertEquals("Backup salvo na pasta.", viewModel.message.value)
    }

    @Test
    fun `uploadCloudBackup should show error message on failure`() = runTest {
        coEvery { cloudSyncRepository.uploadBackup() } returns Result.failure(Exception("Network error"))

        viewModel.uploadCloudBackup()

        assertEquals("Erro ao salvar backup: Network error", viewModel.message.value)
    }

    @Test
    fun `restoreCloudBackup should sync geofences on success`() = runTest {
        coEvery { cloudSyncRepository.restoreBackup() } returns Result.success(Unit)

        viewModel.restoreCloudBackup()

        coVerify { syncGeofencesUseCase() }
        coVerify { widgetRefresher.refresh() }
        assertEquals("Backup restaurado com sucesso!", viewModel.message.value)
    }

    @Test
    fun `connectCloudFolder should show success message`() = runTest {
        val uri = mockk<android.net.Uri>()
        coEvery {
            cloudSyncRepository.connectFolder(uri, CloudStorageProvider.GOOGLE_DRIVE)
        } returns Result.success(Unit)

        viewModel.connectCloudFolder(uri)

        assertEquals("Pasta de backup escolhida.", viewModel.message.value)
    }

    @Test
    fun `signOutCloud should show disconnected message`() = runTest {
        coEvery { cloudSyncRepository.signOut() } returns Unit

        viewModel.signOutCloud()

        assertEquals("Pasta desconectada.", viewModel.message.value)
    }

    @Test
    fun `clearMessage should reset message`() = runTest {
        val file = mockk<File>()
        coEvery { backupManager.importFromFile(file) } returns Result.success(Unit)
        viewModel.importBackup(file)

        viewModel.clearMessage()

        assertNull(viewModel.message.value)
    }

    @Test
    fun `cancel restore does not import backup`() = runTest {
        val file = mockk<File>(relaxed = true)
        viewModel.prepareFileRestore(file)

        viewModel.cancelRestore()

        coVerify(exactly = 0) { backupManager.importFromFile(any()) }
        io.mockk.verify { file.delete() }
        assertNull(viewModel.pendingRestore.value)
    }

    @Test
    fun `confirm file restore imports backup`() = runTest {
        val file = mockk<File>()
        coEvery { backupManager.importFromFile(file) } returns Result.success(Unit)
        viewModel.prepareFileRestore(file)

        viewModel.confirmRestore()

        coVerify { backupManager.importFromFile(file) }
        assertNull(viewModel.pendingRestore.value)
    }

    @Test
    fun `confirm folder restore downloads backup`() = runTest {
        coEvery { cloudSyncRepository.restoreBackup() } returns Result.success(Unit)
        viewModel.prepareFolderRestore()

        viewModel.confirmRestore()

        coVerify { cloudSyncRepository.restoreBackup() }
        assertNull(viewModel.pendingRestore.value)
    }
}
