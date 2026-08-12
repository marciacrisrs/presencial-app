package com.presencial.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
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
    val message by viewModel.message.collectAsStateWithLifecycle()
    val (foregroundPermissions, backgroundPermission) = rememberWorkLocationPermissions()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberSettingsExportLauncher(context, viewModel)
    val importLauncher = rememberSettingsImportLauncher(context, viewModel)

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
            onPercentageSelected = viewModel::updatePercentage,
            onToggleSaturdays = viewModel::updateSaturdays,
            onOpenAiApiKeyChange = viewModel::updateOpenAiApiKey,
            onExport = { exportLauncher.launch("presencial_backup.json") },
            onRestore = { importLauncher.launch(arrayOf("application/json")) },
            onNavigateToAbsences = onNavigateToAbsences,
            onNavigateToWorkAddresses = onNavigateToWorkAddresses,
            onNavigateToAbout = onNavigateToAbout,
            snackbarHostState = snackbarHostState
        )
    )
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
        viewModel.importBackup(temp)
    }
}

private data class SettingsScaffoldParams(
    val settings: com.presencial.app.domain.model.AppSettings,
    val activeWorkAddressCount: Int,
    val foregroundGranted: Boolean,
    val backgroundGranted: Boolean,
    val onPercentageSelected: (Int) -> Unit,
    val onToggleSaturdays: (Boolean) -> Unit,
    val onOpenAiApiKeyChange: (String) -> Unit,
    val onExport: () -> Unit,
    val onRestore: () -> Unit,
    val onNavigateToAbsences: () -> Unit,
    val onNavigateToWorkAddresses: () -> Unit,
    val onNavigateToAbout: () -> Unit,
    val snackbarHostState: SnackbarHostState
)

@Composable
private fun SettingsScaffold(
    params: SettingsScaffoldParams
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SettingsContent(
            params = SettingsContentParams(
                settings = params.settings,
                activeWorkAddressCount = params.activeWorkAddressCount,
                foregroundGranted = params.foregroundGranted,
                backgroundGranted = params.backgroundGranted,
                onPercentageSelected = params.onPercentageSelected,
                onToggleSaturdays = params.onToggleSaturdays,
                onOpenAiApiKeyChange = params.onOpenAiApiKeyChange,
                onExport = params.onExport,
                onRestore = params.onRestore,
                onNavigateToAbsences = params.onNavigateToAbsences,
                onNavigateToWorkAddresses = params.onNavigateToWorkAddresses,
                onNavigateToAbout = params.onNavigateToAbout
            )
        )
        SnackbarHost(hostState = params.snackbarHostState)
    }
}

private data class SettingsContentParams(
    val settings: com.presencial.app.domain.model.AppSettings,
    val activeWorkAddressCount: Int,
    val foregroundGranted: Boolean,
    val backgroundGranted: Boolean,
    val onPercentageSelected: (Int) -> Unit,
    val onToggleSaturdays: (Boolean) -> Unit,
    val onOpenAiApiKeyChange: (String) -> Unit,
    val onExport: () -> Unit,
    val onRestore: () -> Unit,
    val onNavigateToAbsences: () -> Unit,
    val onNavigateToWorkAddresses: () -> Unit,
    val onNavigateToAbout: () -> Unit
)

@Composable
private fun SettingsContent(params: SettingsContentParams) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(PADDING_SCREEN.dp),
        verticalArrangement = Arrangement.spacedBy(SPACING_ITEMS.dp)
    ) {
        Text("Configurações", style = MaterialTheme.typography.headlineLarge)

        MonitoringStatusBanner(
            activeAddressCount = params.activeWorkAddressCount,
            foregroundGranted = params.foregroundGranted,
            backgroundGranted = params.backgroundGranted
        )

        PercentageConfigCard(
            currentPercentage = params.settings.requiredPercentage,
            onPercentageSelected = params.onPercentageSelected
        )

        SaturdaysConfigCard(
            countSaturdays = params.settings.countSaturdaysAsWorkdays,
            onToggle = params.onToggleSaturdays
        )

        AiConfigCard(
            apiKey = params.settings.openAiApiKey,
            onApiKeyChange = params.onOpenAiApiKeyChange
        )

        BackupRestoreCard(
            onExport = params.onExport,
            onRestore = params.onRestore
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
private fun PercentageConfigCard(
    currentPercentage: Int,
    onPercentageSelected: (Int) -> Unit
) {
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
            Text("Percentual obrigatório", style = MaterialTheme.typography.titleLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SPACING_CHIPS.dp)
            ) {
                listOf(PCT_20, PCT_40, PCT_60).forEach { pct ->
                    FilterChip(
                        selected = currentPercentage == pct,
                        onClick = { onPercentageSelected(pct) },
                        label = { Text("$pct%") }
                    )
                }
            }
        }
    }
}

private const val PCT_20 = 20
private const val PCT_40 = 40
private const val PCT_60 = 60

@Composable
private fun SaturdaysConfigCard(
    countSaturdays: Boolean,
    onToggle: (Boolean) -> Unit
) {
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
            Switch(
                checked = countSaturdays,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun AiConfigCard(
    apiKey: String,
    onApiKeyChange: (String) -> Unit
) {
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
            Text("Inteligência (ChatGPT)", style = MaterialTheme.typography.titleLarge)
            Text(
                "Opcional. Informe sua chave OpenAI para mensagens contextuais no dashboard. " +
                    "Sem chave, o app usa recomendações locais.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = ALPHA_ON_SURFACE_MEDIUM)
            )
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Chave da API OpenAI") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
        }
    }
}

@Composable
private fun BackupRestoreCard(
    onExport: () -> Unit,
    onRestore: () -> Unit
) {
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
            Text("Backup e restauração", style = MaterialTheme.typography.titleLarge)
            Button(
                onClick = onExport,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Backup, contentDescription = null)
                Text("  Exportar backup JSON")
            }
            Button(
                onClick = onRestore,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Restore, contentDescription = null)
                Text("  Restaurar backup")
            }
        }
    }
}

@Composable
private fun OtherSettingsCard(
    onAbsences: () -> Unit,
    onWorkAddresses: () -> Unit,
    onAbout: () -> Unit
) {
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
private const val SPACING_CHIPS = 16
private const val CORNER_RADIUS_CARD = 20
private const val ALPHA_SURFACE_VARIANT = 0.4f
private const val ALPHA_ON_SURFACE_MEDIUM = 0.6f
private const val BOTTOM_SPACER_HEIGHT = 16
