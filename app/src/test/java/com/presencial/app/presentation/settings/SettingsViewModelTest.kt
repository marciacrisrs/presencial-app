package com.presencial.app.presentation.settings

import app.cash.turbine.test
import com.presencial.app.data.backup.BackupManager
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.repository.SettingsRepository
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
    private lateinit var viewModel: SettingsViewModel

    @BeforeEach
    fun setup() {
        every { settingsRepository.settings } returns flowOf(AppSettings())
        viewModel = SettingsViewModel(settingsRepository, backupManager)
    }

    @Test
    fun `settings should reflect repository flow`() = runTest {
        val appSettings = AppSettings(requiredPercentage = 50, countSaturdaysAsWorkdays = true)
        every { settingsRepository.settings } returns flowOf(appSettings)
        
        viewModel = SettingsViewModel(settingsRepository, backupManager)

        viewModel.settings.test {
            assertEquals(appSettings, awaitItem())
        }
    }

    @Test
    fun `updatePercentage should call repository`() = runTest {
        coEvery { settingsRepository.updateRequiredPercentage(60) } returns Unit
        
        viewModel.updatePercentage(60)
        
        coVerify { settingsRepository.updateRequiredPercentage(60) }
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
    fun `importBackup should show success message on success`() = runTest {
        val file = mockk<File>()
        coEvery { backupManager.importFromFile(file) } returns Result.success(Unit)

        viewModel.importBackup(file)

        assertEquals("Backup restaurado com sucesso!", viewModel.message.value)
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
