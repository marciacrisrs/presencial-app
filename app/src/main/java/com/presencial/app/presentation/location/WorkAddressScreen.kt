package com.presencial.app.presentation.location

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.presencial.app.domain.model.WorkAddress

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun WorkAddressScreen(
    viewModel: WorkAddressViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()
    val editingAddress by viewModel.editingAddress.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showBackgroundRationale by remember { mutableStateOf(false) }

    val foregroundPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val backgroundPermissionState = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    )

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Locais de Trabalho") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.startEditing(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Local")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (!foregroundPermissionsState.allPermissionsGranted) {
                PermissionCard(
                    title = "Localização Necessária",
                    description = "Para o check-in automático funcionar, precisamos de acesso à sua localização.",
                    buttonText = "Conceder Permissão",
                    onClick = { foregroundPermissionsState.launchMultiplePermissionRequest() }
                )
            } else if (!backgroundPermissionState.allPermissionsGranted) {
                PermissionCard(
                    title = "Localização em Background",
                    description = "O check-in automático só funciona se o app puder acessar a localização \"O tempo todo\". Isso permite registrar sua presença mesmo com o celular no bolso.",
                    buttonText = "Configurar 'O tempo todo'",
                    onClick = { showBackgroundRationale = true }
                )
            }

            if (addresses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum local cadastrado", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(addresses) { address ->
                        WorkAddressItem(
                            address = address,
                            onDelete = { viewModel.deleteAddress(address) },
                            onToggle = { viewModel.toggleActive(address) },
                            onEdit = { viewModel.startEditing(address) }
                        )
                    }
                }
            }
        }
    }

    if (showBackgroundRationale) {
        AlertDialog(
            onDismissRequest = { showBackgroundRationale = false },
            title = { Text("Permissão em Background") },
            text = { Text("Para habilitar o check-in automático, selecione 'Permitir o tempo todo' na próxima tela de configurações.") },
            confirmButton = {
                Button(onClick = {
                    showBackgroundRationale = false
                    backgroundPermissionState.launchMultiplePermissionRequest()
                }) {
                    Text("Entendi")
                }
            }
        )
    }

    editingAddress?.let { address ->
        WorkAddressDialog(
            address = address,
            onDismiss = viewModel::stopEditing,
            onConfirm = { name, addressText, radius, useCurrent ->
                if (useCurrent) {
                    viewModel.saveCurrentLocationAsWorkAddress(name, addressText, radius)
                } else if (address.id != 0L) {
                    viewModel.saveAddress(address.copy(name = name, addressText = addressText, radius = radius))
                } else {
                    viewModel.saveAddress(WorkAddress(name = name, addressText = addressText, latitude = 0.0, longitude = 0.0, radius = radius))
                }
            },
            permissionsGranted = foregroundPermissionsState.allPermissionsGranted
        )
    }
}

@Composable
fun PermissionCard(title: String, description: String, buttonText: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(description)
            Button(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun WorkAddressItem(
    address: WorkAddress,
    onDelete: () -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = address.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = address.addressText, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Raio: ${address.radius.toInt()}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = address.isActive, onCheckedChange = { onToggle() })
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun WorkAddressDialog(
    address: WorkAddress?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Float, Boolean) -> Unit,
    permissionsGranted: Boolean
) {
    var name by remember { mutableStateOf(address?.name ?: "") }
    var addressText by remember { mutableStateOf(address?.addressText ?: "") }
    var radius by remember { mutableStateOf(address?.radius ?: 50f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (address?.id == 0L || address == null) "Novo Local" else "Editar Local") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome (ex: Escritório)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = addressText,
                    onValueChange = { addressText = it },
                    label = { Text("Endereço (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Raio de Ativação: ${radius.toInt()} metros", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = radius,
                    onValueChange = { radius = it },
                    valueRange = 50f..500f,
                    steps = 9 // 50, 100, 150, ..., 500
                )

                if (address?.id == 0L || address == null) {
                    if (permissionsGranted) {
                        Text("O local será definido com base na sua posição atual.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text("Conceda permissão de localização para salvar este local.", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Text("Localização atualizada automaticamente se salvar agora.", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, addressText, radius, address?.id == 0L || address == null) },
                enabled = name.isNotBlank() && (permissionsGranted || address?.id != 0L)
            ) {
                Text(if (address?.id == 0L || address == null) "Salvar Local Atual" else "Atualizar Local")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
