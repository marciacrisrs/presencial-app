package com.presencial.app.presentation.statistics

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.presencial.app.R
import com.presencial.app.domain.usecase.StatisticsData
import com.presencial.app.ui.components.MonthlyBarChart
import com.presencial.app.ui.components.StatSummaryRow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.YearMonth

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val stats by viewModel.statistics.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val message = remember { MutableStateFlow<String?>(null) }

    val pdfLauncher = rememberStatisticsPdfLauncher(context, viewModel, message)

    LaunchedEffect(message) {
        message.collect { msg ->
            msg?.let {
                snackbarHostState.showSnackbar(it)
                message.value = null
            }
        }
    }

    StatisticsScaffold(
        stats = stats,
        onExportClick = {
            val fileName = "presencial_stats_${YearMonth.now()}.pdf"
            pdfLauncher.launch(fileName)
        },
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun rememberStatisticsPdfLauncher(
    context: android.content.Context,
    viewModel: StatisticsViewModel,
    message: MutableStateFlow<String?>
) = rememberLauncherForActivityResult(
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

@Composable
private fun StatisticsScaffold(
    stats: StatisticsData?,
    onExportClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (stats == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            StatisticsContent(
                data = stats,
                onExportClick = onExportClick
            )
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun StatisticsContent(
    data: StatisticsData,
    onExportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Estatísticas", style = MaterialTheme.typography.headlineLarge)

        StatisticsGrid(data)

        MonthlyBarChart(summaries = data.monthlySummaries.sortedBy { it.yearMonth })

        Text(
            "Total presencial: ${data.totalPresencial} dias",
            style = MaterialTheme.typography.bodyLarge
        )

        Button(
            onClick = onExportClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.PictureAsPdf,
                contentDescription = stringResource(R.string.statistics_export_pdf_content_description)
            )
            Text("  Exportar PDF")
        }
    }
}

@Composable
private fun StatisticsGrid(data: StatisticsData) {
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
}
