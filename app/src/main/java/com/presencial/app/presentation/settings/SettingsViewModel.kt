package com.presencial.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presencial.app.domain.model.CloudSyncState
import com.presencial.app.domain.repository.CloudSyncRepository
import com.presencial.app.domain.repository.UserPreferencesRepository
import com.presencial.app.domain.usecase.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.OutputStream
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val backupManager: BackupManager,
    private val cloudSyncRepository: CloudSyncRepository
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _pendingRestore = MutableStateFlow<PendingRestore?>(null)
    val pendingRestore: StateFlow<PendingRestore?> = _pendingRestore.asStateFlow()

    val cloudSyncState: StateFlow<CloudSyncState> = cloudSyncRepository.state

    fun clearMessage() {
        _message.value = null
    }

    fun connectCloudFolder(uri: android.net.Uri) {
        viewModelScope.launch {
            cloudSyncRepository.connectFolder(uri)
                .onSuccess { _message.value = "Pasta de backup escolhida." }
                .onFailure { _message.value = "Erro ao escolher pasta: ${it.message}" }
        }
    }

    fun signOutCloud() {
        viewModelScope.launch {
            cloudSyncRepository.signOut()
            _message.value = "Pasta desconectada."
        }
    }

    fun uploadCloudBackup() {
        viewModelScope.launch {
            cloudSyncRepository.upload()
                .onSuccess { _message.value = "Backup enviado com sucesso!" }
                .onFailure { _message.value = "Erro ao enviar backup: ${it.message}" }
        }
    }

    fun restoreCloudBackup() {
        viewModelScope.launch {
            cloudSyncRepository.restore()
                .onSuccess { _message.value = "Backup restaurado com sucesso!" }
                .onFailure { _message.value = "Erro ao restaurar: ${it.message}" }
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

    fun prepareFileRestore(file: File) {
        _pendingRestore.value = PendingRestore.File(file)
    }

    fun prepareFolderRestore() {
        _pendingRestore.value = PendingRestore.Folder
    }

    fun cancelRestore() {
        val pending = _pendingRestore.value
        if (pending is PendingRestore.File) {
            val deleted = pending.file.delete() || !pending.file.exists()
            if (!deleted) {
                _message.value = "Não foi possível limpar o arquivo temporário do backup."
            }
        }
        _pendingRestore.value = null
    }

    fun confirmRestore() {
        val pending = _pendingRestore.value ?: return
        _pendingRestore.value = null
        when (pending) {
            is PendingRestore.File -> importBackup(pending.file)
            PendingRestore.Folder -> restoreCloudBackup()
        }
    }

    fun importBackup(file: File) {
        viewModelScope.launch {
            backupManager.importFromFile(file)
                .onSuccess { _message.value = "Backup restaurado com sucesso!" }
                .onFailure { _message.value = "Erro ao restaurar: ${it.message}" }
        }
    }
}

sealed interface PendingRestore {
    data class File(val file: java.io.File) : PendingRestore
    data object Folder : PendingRestore
}
