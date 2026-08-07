package com.presencial.app.presentation.location

import android.Manifest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.presencial.app.domain.model.WorkAddress
import com.presencial.app.presentation.location.components.WorkAddressContent
import com.presencial.app.presentation.location.components.WorkAddressContentParams
import com.presencial.app.presentation.location.components.WorkAddressDialogParams
import com.presencial.app.presentation.location.components.WorkAddressDialogs
import com.presencial.app.presentation.location.components.WorkAddressTopBar
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
    var showBackgroundDialog by remember { mutableStateOf(false) }

    val foregroundPermissions = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )
    val backgroundPermission = rememberMultiplePermissionsState(
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            emptyList()
        }
    )

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = { WorkAddressTopBar(onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            WorkAddressFAB(onClick = { viewModel.startEditing(null) })
        }
    ) { padding ->
        WorkAddressContent(
            params = WorkAddressContentParams(
                padding = padding,
                foregroundPermissions = foregroundPermissions,
                backgroundPermission = backgroundPermission,
                addresses = addresses,
                onBackgroundClick = { showBackgroundDialog = true },
                viewModel = viewModel
            )
        )
    }

    WorkAddressDialogsWrapper(
        params = WorkAddressDialogsParams(
            showBackgroundDialog = showBackgroundDialog,
            editingAddress = editingAddress,
            foregroundPermissions = foregroundPermissions,
            backgroundPermission = backgroundPermission,
            onDismissBackground = { showBackgroundDialog = false },
            viewModel = viewModel
        )
    )
}

@Composable
private fun WorkAddressFAB(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(Icons.Default.Add, contentDescription = "Adicionar")
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private data class WorkAddressDialogsParams(
    val showBackgroundDialog: Boolean,
    val editingAddress: WorkAddress?,
    val foregroundPermissions: MultiplePermissionsState,
    val backgroundPermission: MultiplePermissionsState,
    val onDismissBackground: () -> Unit,
    val viewModel: WorkAddressViewModel
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun WorkAddressDialogsWrapper(
    params: WorkAddressDialogsParams
) {
    WorkAddressDialogs(
        params = WorkAddressDialogParams(
            showBackgroundDialog = params.showBackgroundDialog,
            editingAddress = params.editingAddress,
            foregroundPermissions = params.foregroundPermissions,
            onDismissBackground = params.onDismissBackground,
            onConfirmBackground = {
                params.backgroundPermission.launchMultiplePermissionRequest()
            },
            onStopEditing = params.viewModel::stopEditing,
            onSaveAddress = { result ->
                handleSaveAddress(
                    SaveAddressParams(
                        viewModel = params.viewModel,
                        address = params.editingAddress,
                        name = result.name,
                        addressText = result.addressText,
                        radius = result.radius,
                        useCurrent = result.isNew
                    )
                )
            }
        )
    )
}

private data class SaveAddressParams(
    val viewModel: WorkAddressViewModel,
    val address: WorkAddress?,
    val name: String,
    val addressText: String,
    val radius: Float,
    val useCurrent: Boolean
)

private fun handleSaveAddress(params: SaveAddressParams) {
    if (params.useCurrent) {
        params.viewModel.saveCurrentLocationAsWorkAddress(
            params.name,
            params.addressText,
            params.radius
        )
    } else {
        params.address?.let {
            params.viewModel.saveAddress(
                it.copy(
                    name = params.name,
                    addressText = params.addressText,
                    radius = params.radius
                )
            )
        }
    }
}
