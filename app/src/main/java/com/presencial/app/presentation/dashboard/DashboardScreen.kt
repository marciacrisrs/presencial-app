package com.presencial.app.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.presencial.app.ui.components.*
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    openCheckIn: Boolean = false,
    onCheckInHandled: () -> Unit = {}
) {
    val data by viewModel.dashboardData.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

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
            DashboardSkeleton()
            return@Column
        }

        val dashboard = data!!
        val monthName = dashboard.yearMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
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
        }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 60 })
        ) {
            SmartMessageCard(
                message = dashboard.smartMessage,
                isLoading = dashboard.isLoadingAi
            )
        }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 80 })
        ) {
            CircularProgressCard(
                progress = dashboard.progressFraction,
                label = "Meta: ${dashboard.requiredPercentage}%",
                modifier = Modifier.fillMaxWidth()
            )
        }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 100 })
        ) {
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
        }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 120 })
        ) {
            DashboardProgressBar(data = dashboard)
        }

        if (dashboard.todayIsWorkday) {
            CheckInButton(
                isPresencial = dashboard.todayIsPresencial,
                onConfirm = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.toggleTodayCheckIn(true)
                }
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
fun DashboardSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            ShimmerBox(height = 32.dp, widthFraction = 0.1f, shape = RoundedCornerShape(8.dp))
            ShimmerBox(height = 24.dp, widthFraction = 0.4f)
        }
        ShimmerBox(height = 60.dp)
        ShimmerBox(height = 200.dp, shape = RoundedCornerShape(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShimmerBox(height = 80.dp, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
            ShimmerBox(height = 80.dp, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
            ShimmerBox(height = 80.dp, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
        }
        ShimmerBox(height = 20.dp)
    }
}

@Composable
private fun YesterdayCheckInCard(onConfirm: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Esqueceu de ontem?", style = MaterialTheme.typography.titleSmall)
                Text("Registre sua presença anterior", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onConfirm) {
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
