package com.presencial.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import com.presencial.app.domain.model.CloudStorageProvider
import com.presencial.app.domain.model.CloudSyncState
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CloudSyncCard(
    state: CloudSyncState,
    onProviderSelected: (CloudStorageProvider) -> Unit,
    onConnectFolder: () -> Unit,
    onSignOut: () -> Unit,
    onUpload: () -> Unit,
    onRestore: () -> Unit,
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

            Text(stringResource(R.string.cloud_sync_provider_label), style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CloudStorageProvider.entries.forEach { provider ->
                    FilterChip(
                        selected = state.provider == provider,
                        onClick = { onProviderSelected(provider) },
                        label = { Text(provider.displayName) },
                        enabled = !state.isSignedIn && !state.isSyncing
                    )
                }
            }

            StatusText(state)

            val syncingDescription = stringResource(R.string.cloud_sync_syncing)

            if (state.isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .semantics { contentDescription = syncingDescription }
                )
            }

            if (state.isSignedIn) {
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
                Button(onClick = onRestore, enabled = !state.isSyncing, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        Icons.Default.CloudDownload,
                        contentDescription = stringResource(R.string.cloud_sync_restore_icon)
                    )
                    Text("  ${stringResource(R.string.cloud_sync_restore)}")
                }
            } else {
                Button(onClick = onConnectFolder, enabled = !state.isSyncing, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = stringResource(R.string.cloud_sync_connect_icon)
                    )
                    Text("  ${stringResource(R.string.cloud_sync_connect_folder, state.provider.displayName)}")
                }
            }
        }
    }
}

@Composable
private fun StatusText(state: CloudSyncState) {
    val accountLine = if (state.isSignedIn && !state.accountEmail.isNullOrBlank()) {
        stringResource(R.string.cloud_sync_folder_connected, state.accountEmail)
    } else {
        stringResource(R.string.cloud_sync_not_connected)
    }
    Text(accountLine, style = MaterialTheme.typography.bodySmall)

    val lastSync = state.lastSyncEpochMillis?.let { epoch ->
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(epoch))
    } ?: stringResource(R.string.cloud_sync_never)
    Text(
        stringResource(R.string.cloud_sync_last_sync, lastSync),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
}
