package com.presencial.app.presentation.absence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.presencial.app.presentation.absence.components.AbsenceItem
import com.presencial.app.presentation.absence.components.AddAbsenceDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbsenceScreen(
    viewModel: AbsenceViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val absences by viewModel.absences.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = { AbsenceTopBar(onBack) },
        floatingActionButton = {
            AbsenceFAB(onClick = { showAddDialog = true })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        if (absences.isEmpty()) {
            AbsenceEmptyState(padding)
        } else {
            AbsenceList(
                absences = absences,
                padding = padding,
                onDelete = viewModel::deleteAbsence
            )
        }
    }

    if (showAddDialog) {
        AbsenceAddDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { type, start, end, notes ->
                viewModel.addAbsence(type, start, end, notes = notes)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AbsenceTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text("Ausências") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
        }
    )
}

@Composable
private fun AbsenceFAB(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(Icons.Default.Add, contentDescription = "Adicionar Ausência")
    }
}

@Composable
private fun AbsenceEmptyState(padding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text("Nenhuma ausência registrada", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun AbsenceList(
    absences: List<com.presencial.app.domain.model.Absence>,
    padding: PaddingValues,
    onDelete: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SCREEN_PADDING),
        verticalArrangement = Arrangement.spacedBy(ITEM_SPACING)
    ) {
        item { Spacer(modifier = Modifier.height(padding.calculateTopPadding())) }
        items(absences) { absence ->
            AbsenceItem(
                absence = absence,
                onDelete = { onDelete(absence.id) }
            )
        }
        item { Spacer(modifier = Modifier.height(FAB_BOTTOM_SPACER)) }
    }
}

@Composable
private fun AbsenceAddDialog(
    onDismiss: () -> Unit,
    onConfirm: (com.presencial.app.domain.model.AbsenceType, java.time.LocalDate, java.time.LocalDate, String?) -> Unit
) {
    AddAbsenceDialog(
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

private val SCREEN_PADDING = 16.dp
private val ITEM_SPACING = 12.dp
private val FAB_BOTTOM_SPACER = 80.dp
