package com.presencial.app.presentation.location

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
            AnimatedVisibility(
                visible = !foregroundPermissionsState.allPermissionsGranted,
                enter = expandVertically() + fadeIn()
            ) {
                PermissionCard(
                    title = "Localização Necessária",
                    description = "Para o check-in automático funcionar, precisamos de acesso à sua localização.",
                    buttonText = "Conceder Permissão",
                    onClick = { foregroundPermissionsState.launchMultiplePermissionRequest() }
                )
            }

            AnimatedVisibility(
                visible = foregroundPermissionsState.allPermissionsGranted && !backgroundPermissionState.allPermissionsGranted,
                enter = expandVertically() + fadeIn()
            ) {
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
                    itemsIndexed(addresses, key = { _, it -> it.id }) { index, address ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(400, delayMillis = index * 50)) +
                                    slideInHorizontally(animationSpec = tween(400, delayMillis = index * 50)) { -it / 4 }
                        ) {
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
                } else {
                    viewModel.saveAddress(address.copy(name = name, addressText = addressText, radius = radius))
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
    val isNewAddress = address == null || address.id == 0L

    var name by remember { mutableStateOf(address?.name.orEmpty()) }
    var addressText by remember { mutableStateOf(address?.addressText.orEmpty()) }
    var radius by remember { mutableStateOf(address?.radius ?: 50f) }

    val dialogTitle = if (isNewAddress) "Novo Local" else "Editar Local"
    val confirmButtonText =
        if (isNewAddress) "Salvar Local Atual" else "Atualizar Local"

    val canSave =
        name.isNotBlank() && (permissionsGranted || !isNewAddress)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(dialogTitle)
        },
        text = {
            WorkAddressDialogContent(
                name = name,
                onNameChange = { name = it },
                addressText = addressText,
                onAddressChange = { addressText = it },
                radius = radius,
                onRadiusChange = { radius = it },
                isNewAddress = isNewAddress,
                permissionsGranted = permissionsGranted
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        name,
                        addressText,
                        radius,
                        isNewAddress
                    )
                },
                enabled = canSave
            ) {
                Text(confirmButtonText)
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
private fun WorkAddressDialogContent(
    name: String,
    onNameChange: (String) -> Unit,
    addressText: String,
    onAddressChange: (String) -> Unit,
    radius: Float,
    onRadiusChange: (Float) -> Unit,
    isNewAddress: Boolean,
    permissionsGranted: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nome (ex: Escritório)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = addressText,
            onValueChange = onAddressChange,
            label = { Text("Endereço (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Raio de Ativação: ${radius.toInt()} metros",
            style = MaterialTheme.typography.labelMedium
        )

        Slider(
            value = radius,
            onValueChange = onRadiusChange,
            valueRange = 50f..500f,
            steps = 9
        )

        AddressMessage(
            isNewAddress = isNewAddress,
            permissionsGranted = permissionsGranted
        )
    }
}

@Composable
private fun AddressMessage(
    isNewAddress: Boolean,
    permissionsGranted: Boolean
) {
    when {
        isNewAddress && permissionsGranted -> {
            Text(
                text = "O local será definido com base na sua posição atual.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        isNewAddress -> {
            Text(
                text = "Conceda permissão de localização para salvar este local.",
                color = MaterialTheme.colorScheme.error
            )
        }

        else -> {
            Text(
                text = "Localização atualizada automaticamente se salvar agora.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
