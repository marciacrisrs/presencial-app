package com.presencial.app.presentation.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.presencial.app.R
import com.presencial.app.domain.model.GeofenceSyncStatus
import com.presencial.app.domain.model.WeeklyPolicySummary
import com.presencial.app.presentation.components.MonitoringStatusBanner
import com.presencial.app.presentation.location.rememberWorkLocationPermissions

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToAbout: () -> Unit = {},
    onNavigateToAbsences: () -> Unit = {},
    onNavigateToWorkAddresses: () -> Unit = {}
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val workAddresses by viewModel.workAddresses.collectAsStateWithLifecycle()
    val geofenceSyncStatus by viewModel.geofenceSyncStatus.collectAsStateWithLifecycle()
    val weeklySummaries by viewModel.weeklySummaries.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val policyValidation by viewModel.policyValidation.collectAsStateWithLifecycle()
    val cloudSyncState by viewModel.cloudSyncState.collectAsStateWithLifecycle()
    val pendingRestore by viewModel.pendingRestore.collectAsStateWithLifecycle()
    val (foregroundPermissions, backgroundPermission) = rememberWorkLocationPermissions()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberSettingsExportLauncher(context, viewModel)
    val importLauncher = rememberSettingsImportLauncher(context, viewModel)
    val cloudFolderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { viewModel.connectCloudFolder(it) }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    SettingsScaffold(
        params = SettingsScaffoldParams(
            settings = settings,
            activeWorkAddressCount = workAddresses.count { it.isActive },
            foregroundGranted = foregroundPermissions.allPermissionsGranted,
            backgroundGranted = backgroundPermission.allPermissionsGranted,
            geofenceSyncStatus = geofenceSyncStatus,
            weeklySummaries = weeklySummaries,
            onGeofenceRetry = viewModel::retryGeofenceSync,
            onPresencePolicyChange = viewModel::updatePresencePolicy,
            policyValidation = policyValidation,
            onToggleSaturdays = viewModel::updateSaturdays,
            cloudSyncState = cloudSyncState,
            onCloudConnectFolder = { cloudFolderLauncher.launch(null) },
            onCloudSignOut = viewModel::signOutCloud,
            onCloudUpload = viewModel::uploadCloudBackup,
            onCloudRestore = viewModel::prepareFolderRestore,
            onExport = { exportLauncher.launch("presencial_backup.json") },
            onRestore = { importLauncher.launch(arrayOf("application/json")) },
            onNavigateToAbsences = onNavigateToAbsences,
            onNavigateToWorkAddresses = onNavigateToWorkAddresses,
            onNavigateToAbout = onNavigateToAbout,
            snackbarHostState = snackbarHostState
        )
    )

    if (pendingRestore != null) {
        RestoreConfirmDialog(
            onConfirm = viewModel::confirmRestore,
            onDismiss = viewModel::cancelRestore
        )
    }
}

@Composable
private fun rememberSettingsExportLauncher(
    context: android.content.Context,
    viewModel: SettingsViewModel
) = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("application/json")
) { uri ->
    uri?.let {
        val stream = context.contentResolver.openOutputStream(it)
        viewModel.exportBackup(stream)
    }
}

@Composable
private fun rememberSettingsImportLauncher(
    context: android.content.Context,
    viewModel: SettingsViewModel
) = rememberLauncherForActivityResult(
    ActivityResultContracts.OpenDocument()
) { uri ->
    uri?.let {
        val temp = java.io.File(context.cacheDir, "import_backup.json")
        context.contentResolver.openInputStream(it)?.use { stream ->
            temp.outputStream().use { out -> stream.copyTo(out) }
        }
        viewModel.prepareFileRestore(temp)
    }
}

private data class SettingsScaffoldParams(
    val settings: com.presencial.app.domain.model.AppSettings,
    val activeWorkAddressCount: Int,
    val foregroundGranted: Boolean,
    val backgroundGranted: Boolean,
    val geofenceSyncStatus: GeofenceSyncStatus,
    val weeklySummaries: List<WeeklyPolicySummary>,
    val onGeofenceRetry: () -> Unit,
    val onPresencePolicyChange: (com.presencial.app.domain.model.PresencePolicy) -> Unit,
    val policyValidation: com.presencial.app.domain.model.PolicyValidationResult,
    val onToggleSaturdays: (Boolean) -> Unit,
    val cloudSyncState: com.presencial.app.domain.model.CloudSyncState,
    val onCloudConnectFolder: () -> Unit,
    val onCloudSignOut: () -> Unit,
    val onCloudUpload: () -> Unit,
    val onCloudRestore: () -> Unit,
    val onExport: () -> Unit,
    val onRestore: () -> Unit,
    val onNavigateToAbsences: () -> Unit,
    val onNavigateToWorkAddresses: () -> Unit,
    val onNavigateToAbout: () -> Unit,
    val snackbarHostState: SnackbarHostState
)

@Composable
private fun SettingsScaffold(params: SettingsScaffoldParams) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = params.snackbarHostState) }
    ) { padding ->
        SettingsContent(
            modifier = Modifier.padding(padding),
            params = SettingsContentParams(
                settings = params.settings,
                activeWorkAddressCount = params.activeWorkAddressCount,
                foregroundGranted = params.foregroundGranted,
                backgroundGranted = params.backgroundGranted,
                geofenceSyncStatus = params.geofenceSyncStatus,
                weeklySummaries = params.weeklySummaries,
                onGeofenceRetry = params.onGeofenceRetry,
                onPresencePolicyChange = params.onPresencePolicyChange,
                policyValidation = params.policyValidation,
                onToggleSaturdays = params.onToggleSaturdays,
                cloudSyncState = params.cloudSyncState,
                onCloudConnectFolder = params.onCloudConnectFolder,
                onCloudSignOut = params.onCloudSignOut,
                onCloudUpload = params.onCloudUpload,
                onCloudRestore = params.onCloudRestore,
                onExport = params.onExport,
                onRestore = params.onRestore,
                onNavigateToAbsences = params.onNavigateToAbsences,
                onNavigateToWorkAddresses = params.onNavigateToWorkAddresses,
                onNavigateToAbout = params.onNavigateToAbout
            )
        )
    }
}

private data class SettingsContentParams(
    val settings: com.presencial.app.domain.model.AppSettings,
    val activeWorkAddressCount: Int,
    val foregroundGranted: Boolean,
    val backgroundGranted: Boolean,
    val geofenceSyncStatus: GeofenceSyncStatus,
    val weeklySummaries: List<WeeklyPolicySummary>,
    val onGeofenceRetry: () -> Unit,
    val onPresencePolicyChange: (com.presencial.app.domain.model.PresencePolicy) -> Unit,
    val policyValidation: com.presencial.app.domain.model.PolicyValidationResult,
    val onToggleSaturdays: (Boolean) -> Unit,
    val cloudSyncState: com.presencial.app.domain.model.CloudSyncState,
    val onCloudConnectFolder: () -> Unit,
    val onCloudSignOut: () -> Unit,
    val onCloudUpload: () -> Unit,
    val onCloudRestore: () -> Unit,
    val onExport: () -> Unit,
    val onRestore: () -> Unit,
    val onNavigateToAbsences: () -> Unit,
    val onNavigateToWorkAddresses: () -> Unit,
    val onNavigateToAbout: () -> Unit
)

@Composable
private fun SettingsContent(params: SettingsContentParams, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(PADDING_SCREEN.dp),
        verticalArrangement = Arrangement.spacedBy(SPACING_ITEMS.dp)
    ) {
        Text("Configurações", style = MaterialTheme.typography.headlineLarge)

        MonitoringStatusBanner(
            activeAddressCount = params.activeWorkAddressCount,
            foregroundGranted = params.foregroundGranted,
            backgroundGranted = params.backgroundGranted,
            onClick = params.onNavigateToWorkAddresses
        )

        GeofenceSyncStatusCard(
            status = params.geofenceSyncStatus,
            onRetry = params.onGeofenceRetry
        )

        PresencePolicyCard(
            policy = params.settings.presencePolicy,
            validation = params.policyValidation,
            onPolicyChange = params.onPresencePolicyChange
        )

        WeeklyPolicySummaryCard(summaries = params.weeklySummaries)

        SaturdaysConfigCard(
            countSaturdays = params.settings.countSaturdaysAsWorkdays,
            onToggle = params.onToggleSaturdays
        )

        CloudSyncCard(
            state = params.cloudSyncState,
            actions = CloudSyncCardActions(
                onConnectFolder = params.onCloudConnectFolder,
                onSignOut = params.onCloudSignOut,
                onUpload = params.onCloudUpload,
                onRestore = params.onCloudRestore,
                onExportFile = params.onExport,
                onRestoreFile = params.onRestore
            )
        )

        OtherSettingsCard(
            onAbsences = params.onNavigateToAbsences,
            onWorkAddresses = params.onNavigateToWorkAddresses,
            onAbout = params.onNavigateToAbout
        )

        Spacer(modifier = Modifier.height(BOTTOM_SPACER_HEIGHT.dp))
    }
}

@Composable
private fun RestoreConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.backup_restore_confirm_title)) },
        text = { Text(stringResource(R.string.backup_restore_confirm_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.backup_restore_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.backup_restore_cancel))
            }
        }
    )
}

@Composable
private fun SaturdaysConfigCard(countSaturdays: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        shape = RoundedCornerShape(CORNER_RADIUS_CARD.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = ALPHA_SURFACE_VARIANT)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(PADDING_SCREEN.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Sábados como dias úteis", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Por padrão, sábados não contam como dias úteis",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = ALPHA_ON_SURFACE_MEDIUM)
                )
            }
            Switch(checked = countSaturdays, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun OtherSettingsCard(onAbsences: () -> Unit, onWorkAddresses: () -> Unit, onAbout: () -> Unit) {
    Card(
        shape = RoundedCornerShape(CORNER_RADIUS_CARD.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = ALPHA_SURFACE_VARIANT)
        )
    ) {
        Column(
            modifier = Modifier.padding(PADDING_SCREEN.dp),
            verticalArrangement = Arrangement.spacedBy(SPACING_CARD_CONTENT.dp)
        ) {
            Text("Outros", style = MaterialTheme.typography.titleLarge)
            Button(
                onClick = onAbsences,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Gerenciar Ausências")
            }
            Button(
                onClick = onWorkAddresses,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Locais de Trabalho (Check-in Automático)")
            }
            Button(
                onClick = onAbout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Sobre o Aplicativo")
            }
        }
    }
}

private const val PADDING_SCREEN = 20
private const val SPACING_ITEMS = 20
private const val SPACING_CARD_CONTENT = 12
private const val CORNER_RADIUS_CARD = 20
private const val ALPHA_SURFACE_VARIANT = 0.4f
private const val ALPHA_ON_SURFACE_MEDIUM = 0.6f
private const val BOTTOM_SPACER_HEIGHT = 16
