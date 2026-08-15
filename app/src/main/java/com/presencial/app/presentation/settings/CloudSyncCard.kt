package com.presencial.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.presencial.app.R
import com.presencial.app.domain.model.BackupFolderStatus
import com.presencial.app.domain.model.CloudSyncState
import java.text.DateFormat
import java.util.Date

@Composable
fun CloudSyncCard(
    state: CloudSyncState,
    onConnectFolder: () -> Unit,
    onSignOut: () -> Unit,
    onUpload: () -> Unit,
    onRestore: () -> Unit,
    onExportFile: () -> Unit,
    onRestoreFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.cloud_sync_title), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.cloud_sync_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            StatusText(state)

            val syncingDescription = stringResource(R.string.cloud_sync_syncing)
            if (state.isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .semantics { contentDescription = syncingDescription }
                )
            }

            when (state.folderStatus) {
                BackupFolderStatus.NOT_CHOSEN -> {
                    Button(onClick = onConnectFolder, enabled = !state.isSyncing, modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = stringResource(R.string.cloud_sync_connect_icon)
                        )
                        Text("  ${stringResource(R.string.cloud_sync_connect_folder)}")
                    }
                }
                BackupFolderStatus.PERMISSION_REVOKED -> {
                    Button(onClick = onConnectFolder, enabled = !state.isSyncing, modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = stringResource(R.string.cloud_sync_connect_icon)
                        )
                        Text("  ${stringResource(R.string.cloud_sync_connect_folder)}")
                    }
                    OutlinedButton(onClick = onSignOut, enabled = !state.isSyncing, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.cloud_sync_sign_out))
                    }
                }
                BackupFolderStatus.ACCESSIBLE -> {
                    OutlinedButton(onClick = onSignOut, enabled = !state.isSyncing, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.cloud_sync_sign_out))
                    }
                    Button(onClick = onUpload, enabled = !state.isSyncing, modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = stringResource(R.string.cloud_sync_upload_icon)
                        )
                        Text("  ${stringResource(R.string.cloud_sync_upload)}")
                    }
                    Button(
                        onClick = onRestore,
                        enabled = !state.isSyncing && state.backupExists,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = stringResource(R.string.cloud_sync_restore_icon)
                        )
                        Text("  ${stringResource(R.string.cloud_sync_restore)}")
                    }
                }
            }

            Button(onClick = onExportFile, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    Icons.Default.Backup,
                    contentDescription = stringResource(R.string.backup_export_content_description)
                )
                Text("  ${stringResource(R.string.backup_export_file)}")
            }
            Button(onClick = onRestoreFile, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    Icons.Default.Restore,
                    contentDescription = stringResource(R.string.backup_restore_content_description)
                )
                Text("  ${stringResource(R.string.backup_restore_file)}")
            }
        }
    }
}

@Composable
private fun StatusText(state: CloudSyncState) {
    val statusLine = when (state.folderStatus) {
        BackupFolderStatus.NOT_CHOSEN -> stringResource(R.string.cloud_sync_not_connected)
        BackupFolderStatus.PERMISSION_REVOKED -> stringResource(R.string.cloud_sync_permission_revoked)
        BackupFolderStatus.ACCESSIBLE -> {
            val folderName = state.accountEmail?.takeIf { it.isNotBlank() }
                ?: stringResource(R.string.cloud_sync_connect_folder)
            if (state.backupExists) {
                stringResource(R.string.cloud_sync_folder_connected, folderName)
            } else {
                stringResource(R.string.cloud_sync_backup_missing)
            }
        }
    }
    Text(statusLine, style = MaterialTheme.typography.bodySmall)

    val lastSync = state.lastSyncEpochMillis?.let { epoch ->
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(epoch))
    } ?: stringResource(R.string.cloud_sync_never)
    Text(
        stringResource(R.string.cloud_sync_last_sync, lastSync),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
}
