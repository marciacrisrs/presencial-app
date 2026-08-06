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
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
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
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Local")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (!locationPermissionsState.allPermissionsGranted) {
                Card(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Permissões Necessárias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Para o check-in automático funcionar, precisamos de acesso à sua localização.")
                        Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                            Text("Conceder Permissão")
                        }
                    }
                }
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
                            onToggle = { viewModel.toggleActive(address) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddWorkAddressDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, addressText, useCurrent ->
                if (useCurrent) {
                    viewModel.saveCurrentLocationAsWorkAddress(name, addressText)
                } else {
                    // Logic for manual coordinates could be added here
                    viewModel.saveAddress(WorkAddress(name = name, addressText = addressText, latitude = 0.0, longitude = 0.0))
                }
                showAddDialog = false
            },
            permissionsGranted = locationPermissionsState.allPermissionsGranted
        )
    }
}

@Composable
fun WorkAddressItem(
    address: WorkAddress,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
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
fun AddWorkAddressDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean) -> Unit,
    permissionsGranted: Boolean
) {
    var name by remember { mutableStateOf("") }
    var addressText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Local de Trabalho") },
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
                if (permissionsGranted) {
                    Text("O local será definido com base na sua posição atual.", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("Conceda permissão de localização para salvar este local.", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, addressText, true) },
                enabled = name.isNotBlank() && permissionsGranted
            ) {
                Text("Salvar Local Atual")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
