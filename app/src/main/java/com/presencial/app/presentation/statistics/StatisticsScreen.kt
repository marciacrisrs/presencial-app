package com.presencial.app.presentation.statistics

import android.os.Environment
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.presencial.app.ui.components.MonthlyBarChart
import com.presencial.app.ui.components.StatSummaryRow
import java.io.File
import java.time.YearMonth

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val stats by viewModel.statistics.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
                val file = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "presencial_stats_${YearMonth.now()}.pdf"
                )
                viewModel.exportPdf(file)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
            Text("  Exportar PDF")
        }
    }
}
