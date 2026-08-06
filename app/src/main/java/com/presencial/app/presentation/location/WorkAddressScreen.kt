package com.presencial.app.presentation.location

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.presencial.app.domain.model.WorkAddress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import com.presencial.app.presentation.location.components.WorkAddressDialog
import com.presencial.app.presentation.location.model.WorkAddressViewModel

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun WorkAddressScreen(
    viewModel: WorkAddressViewModel = hiltViewModel(),
    onBack: () -> Unit
) {

    val addresses by viewModel.addresses.collectAsStateWithLifecycle()
    val editingAddress by viewModel.editingAddress.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var showBackgroundDialog by remember {
        mutableStateOf(false)
    }

    val foregroundPermissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val backgroundPermission = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    )

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopBar(onBack)
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.startEditing(null)
                }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Adicionar"
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            PermissionSection(
                foregroundGranted = foregroundPermissions.allPermissionsGranted,
                backgroundGranted = backgroundPermission.allPermissionsGranted,
                onForegroundPermission = {
                    foregroundPermissions.launchMultiplePermissionRequest()
                },
                onBackgroundPermission = {
                    showBackgroundDialog = true
                }
            )

            WorkAddressList(
                addresses = addresses,
                onDelete = viewModel::deleteAddress,
                onToggle = viewModel::toggleActive,
                onEdit = viewModel::startEditing
            )
        }
    }

    if (showBackgroundDialog) {
        BackgroundPermissionDialog(
            onDismiss = {
                showBackgroundDialog = false
            },
            onConfirm = {
                showBackgroundDialog = false
                backgroundPermission.launchMultiplePermissionRequest()
            }
        )
    }

    editingAddress?.let { address ->

        WorkAddressDialog(
            address = address,
            permissionsGranted = foregroundPermissions.allPermissionsGranted,
            onDismiss = viewModel::stopEditing,
            onConfirm = { name, addressText, radius, useCurrent ->

                if (useCurrent) {
                    viewModel.saveCurrentLocationAsWorkAddress(
                        name,
                        addressText,
                        radius
                    )
                } else {
                    viewModel.saveAddress(
                        address.copy(
                            name = name,
                            addressText = addressText,
                            radius = radius
                        )
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text("Locais de Trabalho")
        },
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
                onDelete = {
                    onDelete(address)
                },
                onToggle = {
                    onToggle(address)
                },
                onEdit = {
                    onEdit(address)
                }
            )
        }
    }
}

@Composable
private fun EmptyAddressState() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
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
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
            Text("Para que o app registre seu comparecimento automaticamente, selecione a opção \"Permitir o tempo todo\" na próxima tela de configurações.")
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
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