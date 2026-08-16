package com.presencial.app.presentation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presencial.app.data.backup.BackupManager
import com.presencial.app.domain.model.AppSettings
import com.presencial.app.domain.model.CloudSyncState
import com.presencial.app.domain.model.PolicyValidationResult
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.model.WorkAddress
import com.presencial.app.domain.repository.CloudSyncRepository
import com.presencial.app.domain.repository.SettingsRepository
import com.presencial.app.domain.repository.WorkAddressRepository
import com.presencial.app.domain.usecase.SyncGeofencesUseCase
import com.presencial.app.domain.util.PresencePolicyCalculator
import com.presencial.app.domain.widget.WidgetRefresher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager,
    private val cloudSyncRepository: CloudSyncRepository,
    private val syncGeofencesUseCase: SyncGeofencesUseCase,
    workAddressRepository: WorkAddressRepository,
    private val widgetRefresher: WidgetRefresher
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AppSettings())

    val workAddresses: StateFlow<List<WorkAddress>> = workAddressRepository.getAllAddresses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val policyValidation: StateFlow<PolicyValidationResult> = settings
        .map { PresencePolicyCalculator.validate(it.presencePolicy) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            PolicyValidationResult(isValid = true)
        )

    val cloudSyncState: StateFlow<CloudSyncState> = cloudSyncRepository.syncState

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _pendingRestore = MutableStateFlow<PendingRestore?>(null)
    val pendingRestore: StateFlow<PendingRestore?> = _pendingRestore

    init {
        viewModelScope.launch {
            cloudSyncRepository.refreshState()
        }
    }

    fun updatePresencePolicy(policy: PresencePolicy) {
        viewModelScope.launch {
            val validation = PresencePolicyCalculator.validate(policy.normalized())
            if (!validation.isValid) {
                _message.value = validation.errors.firstOrNull()
                return@launch
            }
            settingsRepository.updatePresencePolicy(policy)
            widgetRefresher.refresh()
        }
    }

    fun updateSaturdays(count: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateCountSaturdaysAsWorkdays(count)
            widgetRefresher.refresh()
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
            val deleted = !pending.file.exists() || pending.file.delete()
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
                .onSuccess {
                    syncGeofencesUseCase()
                    widgetRefresher.refresh()
                    _message.value = "Backup restaurado com sucesso!"
                }
                .onFailure { _message.value = "Erro ao restaurar: ${it.message}" }
        }
    }

    fun connectCloudFolder(treeUri: Uri) {
        viewModelScope.launch {
            val provider = cloudSyncState.value.provider
            cloudSyncRepository.connectFolder(treeUri, provider)
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
            runCatching { cloudSyncRepository.uploadBackup().getOrThrow() }
                .onSuccess { _message.value = "Backup salvo na pasta." }
                .onFailure {
                    _message.value = "Erro ao salvar backup: ${it.message ?: "Erro desconhecido"}"
                }
        }
    }

    fun restoreCloudBackup() {
        viewModelScope.launch {
            runCatching { cloudSyncRepository.restoreBackup().getOrThrow() }
                .onSuccess {
                    syncGeofencesUseCase()
                    widgetRefresher.refresh()
                    _message.value = "Backup restaurado com sucesso!"
                }
                .onFailure {
                    _message.value = "Erro ao restaurar: ${it.message ?: "Erro desconhecido"}"
                }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

private const val STOP_TIMEOUT_MS = 5000L
