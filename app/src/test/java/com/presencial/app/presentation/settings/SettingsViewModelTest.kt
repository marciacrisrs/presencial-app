package com.presencial.app.presentation.settings

import app.cash.turbine.test
import com.presencial.app.data.backup.BackupManager
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.CloudSyncState
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.repository.CloudSyncRepository
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
    private val workAddressRepository = mockk<WorkAddressRepository>()
    private val widgetRefresher = mockk<WidgetRefresher>()
    private lateinit var viewModel: SettingsViewModel

    @BeforeEach
    fun setup() {
        every { settingsRepository.settings } returns flowOf(AppSettings())
        every { workAddressRepository.getAllAddresses() } returns flowOf(emptyList())
        every { cloudSyncRepository.syncState } returns MutableStateFlow(CloudSyncState())
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

        assertEquals("Backup enviado para a nuvem!", viewModel.message.value)
    }

    @Test
    fun `clearMessage should reset message`() = runTest {
        val file = mockk<File>()
        coEvery { backupManager.importFromFile(file) } returns Result.success(Unit)
        viewModel.importBackup(file)

        viewModel.clearMessage()

        assertNull(viewModel.message.value)
    }
}
