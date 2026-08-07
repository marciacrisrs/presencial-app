package com.presencial.app.presentation.location.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.presencial.app.domain.model.WorkAddress
import com.presencial.app.presentation.location.model.WorkAddressViewModel

@OptIn(ExperimentalPermissionsApi::class)
data class WorkAddressDialogParams(
    val showBackgroundDialog: Boolean,
    val editingAddress: WorkAddress?,
    val foregroundPermissions: MultiplePermissionsState,
    val onDismissBackground: () -> Unit,
    val onConfirmBackground: () -> Unit,
    val onStopEditing: () -> Unit,
    val onSaveAddress: (WorkAddressDialogResult) -> Unit
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WorkAddressDialogs(params: WorkAddressDialogParams) {
    if (params.showBackgroundDialog) {
        BackgroundPermissionDialog(
            onDismiss = params.onDismissBackground,
            onConfirm = params.onConfirmBackground
        )
    }

    params.editingAddress?.let { address ->
        WorkAddressDialog(
            address = address,
            permissionsGranted = params.foregroundPermissions.allPermissionsGranted,
            onDismiss = params.onStopEditing,
            onConfirm = params.onSaveAddress
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
data class WorkAddressContentParams(
    val padding: androidx.compose.foundation.layout.PaddingValues,
    val foregroundPermissions: MultiplePermissionsState,
    val backgroundPermission: MultiplePermissionsState,
    val addresses: List<WorkAddress>,
    val onBackgroundClick: () -> Unit,
    val viewModel: WorkAddressViewModel
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WorkAddressContent(params: WorkAddressContentParams) {
    Column(
        modifier = Modifier
            .padding(params.padding)
            .fillMaxSize()
    ) {
        PermissionSection(
            foregroundGranted = params.foregroundPermissions.allPermissionsGranted,
            backgroundGranted = params.backgroundPermission.allPermissionsGranted,
            onForegroundPermission = {
                params.foregroundPermissions.launchMultiplePermissionRequest()
            },
            onBackgroundPermission = params.onBackgroundClick
        )

        WorkAddressList(
            addresses = params.addresses,
            onDelete = params.viewModel::deleteAddress,
            onToggle = params.viewModel::toggleActive,
            onEdit = params.viewModel::startEditing
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkAddressTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text("Locais de Trabalho") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar"
                )
            }
        }
    )
}

@Composable
private fun PermissionSection(
    foregroundGranted: Boolean,
    backgroundGranted: Boolean,
    onForegroundPermission: () -> Unit,
    onBackgroundPermission: () -> Unit
) {
    AnimatedVisibility(
        visible = !foregroundGranted,
        enter = expandVertically() + fadeIn()
    ) {
        PermissionCard(
            title = "Localização Necessária",
            description = "Para o check-in automático funcionar, precisamos de acesso à sua localização.",
            buttonText = "Conceder Permissão",
            onClick = onForegroundPermission
        )
    }

    AnimatedVisibility(
        visible = foregroundGranted && !backgroundGranted,
        enter = expandVertically() + fadeIn()
    ) {
        PermissionCard(
            title = "Localização em Background",
            description = "O check-in automático só funciona se o app puder acessar a localização \"O tempo todo\".",
            buttonText = "Configurar",
            onClick = onBackgroundPermission
        )
    }
}

@Composable
private fun WorkAddressList(
    addresses: List<WorkAddress>,
    onDelete: (WorkAddress) -> Unit,
    onToggle: (WorkAddress) -> Unit,
    onEdit: (WorkAddress) -> Unit
) {
    if (addresses.isEmpty()) {
        EmptyAddressState()
        return
    }

    LazyColumn {
        itemsIndexed(
            addresses,
            key = { _, item -> item.id }
        ) { _, address ->
            WorkAddressItem(
                address = address,
                onDelete = { onDelete(address) },
                onToggle = { onToggle(address) },
                onEdit = { onEdit(address) }
            )
        }
    }
}

@Composable
private fun EmptyAddressState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Nenhum local cadastrado")
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SPACING_MEDIUM),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(SPACING_MEDIUM),
            verticalArrangement = Arrangement.spacedBy(SPACING_SMALL)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}

@Composable
private fun BackgroundPermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Localização \"O tempo todo\"") },
        text = {
            Text(
                "Para que o app registre seu comparecimento automaticamente, " +
                "selecione a opção \"Permitir o tempo todo\" na próxima tela de configurações."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Ir para Configurações")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun WorkAddressItem(
    address: WorkAddress,
    onDelete: () -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SPACING_MEDIUM, vertical = SPACING_SMALL),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .padding(SPACING_MEDIUM)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = address.name, style = MaterialTheme.typography.titleMedium)
                Text(text = address.addressText, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Raio: ${address.radius.toInt()}m",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = address.isActive,
                    onCheckedChange = { onToggle() }
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private val SPACING_MEDIUM = 16.dp
private val SPACING_SMALL = 8.dp
