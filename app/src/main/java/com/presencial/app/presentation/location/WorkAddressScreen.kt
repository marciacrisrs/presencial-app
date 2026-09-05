package com.presencial.app.presentation.location

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.ui.res.stringResource
import com.presencial.app.R
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
import com.presencial.app.domain.location.GeofencePermissionSync
import com.presencial.app.presentation.location.components.WorkAddressContent
import com.presencial.app.presentation.location.components.WorkAddressContentParams
import com.presencial.app.presentation.location.components.WorkAddressDialogParams
import com.presencial.app.presentation.location.components.WorkAddressDialogs
import com.presencial.app.presentation.location.components.WorkAddressTopBar
import com.presencial.app.presentation.location.model.WorkAddressViewModel
import com.presencial.app.presentation.location.rememberWorkLocationPermissions

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
    val isGeocoding by viewModel.isGeocoding.collectAsStateWithLifecycle()
    val geocodedLocation by viewModel.geocodedLocation.collectAsStateWithLifecycle()
    val currentGpsLocation by viewModel.currentGpsLocation.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showBackgroundDialog by remember { mutableStateOf(false) }

    val (foregroundPermissions, backgroundPermission) = rememberWorkLocationPermissions()
    val locationPermissionsGranted = GeofencePermissionSync.shouldSync(
        foregroundGranted = foregroundPermissions.allPermissionsGranted,
        backgroundGranted = backgroundPermission.allPermissionsGranted
    )

    LaunchedEffect(locationPermissionsGranted) {
        if (locationPermissionsGranted) {
            viewModel.syncGeofences()
        }
    }

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
            isGeocoding = isGeocoding,
            geocodedLocation = geocodedLocation,
            currentGpsLocation = currentGpsLocation,
            onDismissBackground = { showBackgroundDialog = false },
            viewModel = viewModel
        )
    )
}

@Composable
private fun WorkAddressFAB(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add))
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private data class WorkAddressDialogsParams(
    val showBackgroundDialog: Boolean,
    val editingAddress: com.presencial.app.domain.model.WorkAddress?,
    val foregroundPermissions: MultiplePermissionsState,
    val backgroundPermission: MultiplePermissionsState,
    val isGeocoding: Boolean,
    val geocodedLocation: Pair<Double, Double>?,
    val currentGpsLocation: Pair<Double, Double>?,
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
            isGeocoding = params.isGeocoding,
            geocodedLocation = params.geocodedLocation,
            currentGpsLocation = params.currentGpsLocation,
            onDismissBackground = params.onDismissBackground,
            onConfirmBackground = {
                params.backgroundPermission.launchMultiplePermissionRequest()
            },
            onStopEditing = params.viewModel::stopEditing,
            onSaveAddress = { result ->
                params.viewModel.saveWorkAddress(
                    id = result.id,
                    name = result.name,
                    addressText = result.addressText,
                    latitude = result.latitude,
                    longitude = result.longitude,
                    radius = result.radius,
                    isActive = result.isActive
                )
            },
            onGeocodeRequest = params.viewModel::geocodeAddress,
            onUseCurrentLocation = params.viewModel::fetchCurrentLocation,
            onLocationConsumed = {
                params.viewModel.consumeGeocodedLocation()
                params.viewModel.consumeCurrentGpsLocation()
            }
        )
    )
}
