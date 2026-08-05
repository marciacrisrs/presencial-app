package com.presencial.app.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.presencial.app.ui.components.CircularProgressCard
import com.presencial.app.ui.components.DashboardProgressBar
import com.presencial.app.ui.components.SmartMessageCard
import com.presencial.app.ui.components.StatCard
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    openCheckIn: Boolean = false,
    onCheckInHandled: () -> Unit = {}
) {
    val data by viewModel.dashboardData.collectAsStateWithLifecycle()

    LaunchedEffect(openCheckIn) {
        if (openCheckIn) onCheckInHandled()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (data == null) {
            CircularProgressIndicator(modifier = Modifier.padding(32.dp))
            return@Column
        }

        val dashboard = data!!
        val monthName = dashboard.yearMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))

        Text(
            text = "${monthName.replaceFirstChar { it.uppercase() }} ${dashboard.yearMonth.year}",
            style = MaterialTheme.typography.headlineLarge
        )

        AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically()) {
            SmartMessageCard(message = dashboard.smartMessage)
        }

        CircularProgressCard(
            progress = dashboard.progressFraction,
            label = "Meta: ${dashboard.requiredPercentage}%",
            modifier = Modifier.fillMaxWidth()
        )

        DashboardProgressBar(data = dashboard)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Dias úteis",
                value = "${dashboard.workdays}",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Obrigatórios",
                value = "${dashboard.requiredDays}",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Realizados",
                value = "${dashboard.completedDays}",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Restantes",
                value = "${dashboard.remainingDays}",
                modifier = Modifier.weight(1f),
                subtitle = "${dashboard.achievedPercentage.toInt()}% atingido"
            )
        }

        if (dashboard.todayIsWorkday) {
            CheckInButton(
                isPresencial = dashboard.todayIsPresencial,
                onConfirm = { viewModel.toggleTodayCheckIn(true) },
                onUndo = { viewModel.toggleTodayCheckIn(false) }
            )
        }

        if (dashboard.yesterdayIsPending) {
            YesterdayCheckInCard(
                onConfirm = viewModel::markYesterdayPresencial
            )
        }

        if (dashboard.completedDays >= dashboard.requiredDays && dashboard.requiredDays > 0) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(com.presencial.app.R.raw.success))
            LottieAnimation(
                composition = composition,
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun YesterdayCheckInCard(onConfirm: () -> Unit) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Esqueceu de ontem?",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Você não registrou sua presença no dia anterior.",
                style = MaterialTheme.typography.bodyMedium
            )
            androidx.compose.material3.TextButton(
                onClick = onConfirm,
                modifier = Modifier.align(androidx.compose.ui.Alignment.End)
            ) {
                Text("Registrar presencial")
            }
        }
    }
}

@Composable
private fun CheckInButton(
    isPresencial: Boolean,
    onConfirm: () -> Unit,
    onUndo: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!isPresencial) {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "✅ Trabalhei presencialmente hoje",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Button(
                onClick = { },
                enabled = false,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Text(
                    text = "  Presença confirmada!",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            OutlinedButton(
                onClick = onUndo,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Undo, contentDescription = null)
                Text("  Desfazer")
            }
        }
    }
}
