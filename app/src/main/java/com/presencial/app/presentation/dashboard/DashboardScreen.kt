package com.presencial.app.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (data == null) {
            CircularProgressIndicator(modifier = Modifier.padding(32.dp))
            return@Column
        }

        val dashboard = data!!
        val monthName = dashboard.yearMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.presencial.app.R.drawable.logo_splash),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "${monthName.replaceFirstChar { it.uppercase() }} ${dashboard.yearMonth.year}",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically()) {
            SmartMessageCard(message = dashboard.smartMessage)
        }

        CircularProgressCard(
            progress = dashboard.progressFraction,
            label = "Meta: ${dashboard.requiredPercentage}%",
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Úteis",
                value = "${dashboard.workdays}",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Meta",
                value = "${dashboard.requiredDays}",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Feito",
                value = "${dashboard.completedDays}",
                modifier = Modifier.weight(1f)
            )
        }

        DashboardProgressBar(data = dashboard)



        if (dashboard.todayIsWorkday) {
            CheckInButton(
                isPresencial = dashboard.todayIsPresencial,
                onConfirm = { viewModel.toggleTodayCheckIn(true) }
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
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Esqueceu de ontem?", style = MaterialTheme.typography.titleSmall)
                Text("Registre sua presença anterior", style = MaterialTheme.typography.bodySmall)
            }
            androidx.compose.material3.TextButton(onClick = onConfirm) {
                Text("Registrar")
            }
        }
    }
}

@Composable
private fun CheckInButton(
    isPresencial: Boolean,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!isPresencial) {
            ElevatedButton(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Registrar Presença Hoje",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            Button(
                onClick = { },
                enabled = false,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Presença Registrada",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
