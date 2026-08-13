package com.presencial.app.presentation.statistics

import android.content.Context
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
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.presencial.app.R
import com.presencial.app.domain.model.DayInfo
import com.presencial.app.domain.usecase.StatisticsData
import com.presencial.app.ui.components.AnnualSummaryCard
import com.presencial.app.ui.components.MonthlyBarChart
import com.presencial.app.ui.components.MonthlyTrendLineChart
import com.presencial.app.ui.components.StatSummaryRow
import com.presencial.app.ui.components.WeeklyBarChart
import com.presencial.app.ui.components.YearHeatmapCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val stats by viewModel.statistics.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val message = remember { MutableStateFlow<String?>(null) }
    val exportFileBaseName = viewModel.exportFileBaseName()
    val exportErrorTemplate = stringResource(R.string.statistics_export_error)

    val pdfLauncher = rememberExportLauncher(
        mimeType = "application/pdf",
        context = context,
        scope = scope,
        onExport = { stream -> viewModel.exportPdf(stream) },
        successMessage = stringResource(R.string.statistics_export_pdf_success),
        errorTemplate = exportErrorTemplate,
        message = message
    )
    val csvLauncher = rememberExportLauncher(
        mimeType = "text/csv",
        context = context,
        scope = scope,
        onExport = { stream -> viewModel.exportCsv(stream) },
        successMessage = stringResource(R.string.statistics_export_csv_success),
        errorTemplate = exportErrorTemplate,
        message = message
    )
    val excelLauncher = rememberExportLauncher(
        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        context = context,
        scope = scope,
        onExport = { stream -> viewModel.exportExcel(stream) },
        successMessage = stringResource(R.string.statistics_export_excel_success),
        errorTemplate = exportErrorTemplate,
        message = message
    )

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
        onExportPdf = { pdfLauncher.launch("$exportFileBaseName.pdf") },
        onExportCsv = { csvLauncher.launch("$exportFileBaseName.csv") },
        onExportExcel = { excelLauncher.launch("$exportFileBaseName.xlsx") },
        onPreviousYear = viewModel::previousYear,
        onNextYear = viewModel::nextYear,
        onDayClick = { dayInfo ->
            message.value = formatDayDetail(dayInfo)
        },
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun rememberExportLauncher(
    mimeType: String,
    context: Context,
    scope: CoroutineScope,
    onExport: suspend (OutputStream) -> Result<Unit>,
    successMessage: String,
    errorTemplate: String,
    message: MutableStateFlow<String?>
) = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument(mimeType)
) { uri ->
    uri?.let {
        scope.launch {
            context.contentResolver.openOutputStream(it)?.use { stream ->
                onExport(stream)
                    .onSuccess { message.value = successMessage }
                    .onFailure { error ->
                        message.value = String.format(
                            errorTemplate,
                            error.message ?: "Erro desconhecido"
                        )
                    }
            }
        }
    }
}

@Composable
private fun StatisticsScaffold(
    stats: StatisticsData?,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    onExportExcel: () -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onDayClick: (DayInfo) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (stats == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            StatisticsContent(
                data = stats,
                onExportPdf = onExportPdf,
                onExportCsv = onExportCsv,
                onExportExcel = onExportExcel,
                onPreviousYear = onPreviousYear,
                onNextYear = onNextYear,
                onDayClick = onDayClick
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
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    onExportExcel: () -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onDayClick: (DayInfo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.statistics_title), style = MaterialTheme.typography.headlineLarge)

        StatisticsGrid(data)

        val yearSummaries = data.monthlySummaries
            .filter { it.yearMonth.year == data.selectedYear }
            .sortedBy { it.yearMonth }

        AnnualSummaryCard(summary = data.annualSummary)

        MonthlyTrendLineChart(summaries = yearSummaries)

        WeeklyBarChart(summaries = data.weeklySummaries)

        MonthlyBarChart(summaries = yearSummaries)

        YearHeatmapCard(
            year = data.selectedYear,
            days = data.heatmapDays,
            onPreviousYear = onPreviousYear,
            onNextYear = onNextYear,
            onDayClick = onDayClick
        )

        Text(
            stringResource(R.string.statistics_total_presencial, data.totalPresencial),
            style = MaterialTheme.typography.bodyLarge
        )

        ExportButtons(
            onExportPdf = onExportPdf,
            onExportCsv = onExportCsv,
            onExportExcel = onExportExcel
        )
    }
}

@Composable
private fun ExportButtons(
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    onExportExcel: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onExportPdf, modifier = Modifier.fillMaxWidth()) {
            Icon(
                Icons.Default.PictureAsPdf,
                contentDescription = stringResource(R.string.statistics_export_pdf_content_description)
            )
            Text("  ${stringResource(R.string.statistics_export_pdf)}")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onExportCsv,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.TableChart,
                    contentDescription = stringResource(R.string.statistics_export_csv_content_description)
                )
                Text("  ${stringResource(R.string.statistics_export_csv)}")
            }
            OutlinedButton(
                onClick = onExportExcel,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.TableChart,
                    contentDescription = stringResource(R.string.statistics_export_excel_content_description)
                )
                Text("  ${stringResource(R.string.statistics_export_excel)}")
            }
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
            label = stringResource(R.string.statistics_average_annual),
            value = "${"%.1f".format(data.annualSummary.averageAchieved)}%",
            modifier = Modifier.weight(1f)
        )
        StatSummaryRow(
            label = stringResource(R.string.statistics_current_streak),
            value = stringResource(R.string.statistics_days_count, data.currentStreak),
            modifier = Modifier.weight(1f)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatSummaryRow(
            label = stringResource(R.string.statistics_longest_streak),
            value = stringResource(R.string.statistics_days_count, data.longestStreak),
            modifier = Modifier.weight(1f)
        )
        StatSummaryRow(
            label = stringResource(R.string.statistics_total_home_office),
            value = "${data.totalHomeOffice}",
            modifier = Modifier.weight(1f)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatSummaryRow(
            label = stringResource(R.string.statistics_total_presencial_label),
            value = "${data.totalPresencial}",
            modifier = Modifier.weight(1f)
        )
        data.annualSummary.bestMonth?.let { best ->
            StatSummaryRow(
                label = stringResource(R.string.statistics_best_month),
                value = "${"%.0f".format(best.achievedPercentage)}%",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun formatDayDetail(dayInfo: DayInfo): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val weekday = dayInfo.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val status = when (dayInfo.status) {
        com.presencial.app.domain.model.DayStatus.PRESENCIAL -> "Presencial"
        com.presencial.app.domain.model.DayStatus.HOME_OFFICE -> "Home Office"
        com.presencial.app.domain.model.DayStatus.FERIADO -> dayInfo.holidayName ?: "Feriado"
        com.presencial.app.domain.model.DayStatus.FIM_DE_SEMANA -> "Fim de semana"
        com.presencial.app.domain.model.DayStatus.FUTURO -> "Futuro"
        com.presencial.app.domain.model.DayStatus.FALTOU -> "Faltou"
        com.presencial.app.domain.model.DayStatus.ABSENCE -> "Ausência"
    }
    return "${formatter.format(dayInfo.date)} ($weekday): $status"
}
