package com.presencial.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presencial.app.data.backup.BackupManager
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _message = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun updatePercentage(percentage: Int) {
        viewModelScope.launch {
            settingsRepository.updateRequiredPercentage(percentage)
        }
    }

    fun updateSaturdays(count: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateCountSaturdaysAsWorkdays(count)
        }
    }

    fun exportBackup(outputStream: OutputStream?) {
        if (outputStream == null) return
        viewModelScope.launch {
            backupManager.exportToStream(outputStream)
                .onSuccess { _message.value = "Backup exportado com sucesso!" }
                .onFailure { _message.value = "Erro ao exportar: ${it.message}" }
        }
    }

    fun importBackup(file: File) {
        viewModelScope.launch {
            backupManager.importFromFile(file)
                .onSuccess { _message.value = "Backup restaurado com sucesso!" }
                .onFailure { _message.value = "Erro ao restaurar: ${it.message}" }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
