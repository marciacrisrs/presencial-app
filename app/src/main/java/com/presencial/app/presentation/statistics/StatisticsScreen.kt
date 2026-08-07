package com.presencial.app.presentation.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.presencial.app.ui.components.MonthlyBarChart
import com.presencial.app.ui.components.StatSummaryRow
import java.time.YearMonth

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val stats by viewModel.statistics.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val message = remember { MutableStateFlow<String?>(null) }

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { stream ->
                viewModel.exportPdf(stream)
                    .onSuccess { message.value = "PDF exportado com sucesso!" }
                    .onFailure { message.value = "Erro ao exportar PDF: ${it.message}" }
            }
        }
    }

    LaunchedEffect(message) {
        message.collect { msg ->
            msg?.let {
                snackbarHostState.showSnackbar(it)
                message.value = null
            }
        }
    }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Estatísticas", style = MaterialTheme.typography.headlineLarge)

            if (stats == null) {
                CircularProgressIndicator()
                return@Column
            }

            val data = stats!!

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatSummaryRow(
                    label = "Média anual",
                    value = "${"%.1f".format(data.averageAchieved)}%",
                    modifier = Modifier.weight(1f)
                )
                StatSummaryRow(
                    label = "Sequência atual",
                    value = "${data.currentStreak} dias",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatSummaryRow(
                    label = "Maior sequência",
                    value = "${data.longestStreak} dias",
                    modifier = Modifier.weight(1f)
                )
                StatSummaryRow(
                    label = "Home office",
                    value = "${data.totalHomeOffice}",
                    modifier = Modifier.weight(1f)
                )
            }

            MonthlyBarChart(summaries = data.monthlySummaries.sortedBy { it.yearMonth })

            Text(
                "Total presencial: ${data.totalPresencial} dias",
                style = MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = {
                    val fileName = "presencial_stats_${YearMonth.now()}.pdf"
                    pdfLauncher.launch(fileName)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Text("  Exportar PDF")
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
        )
    }
}
