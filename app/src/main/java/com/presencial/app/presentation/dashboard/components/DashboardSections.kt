package com.presencial.app.presentation.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.presencial.app.R
import com.presencial.app.domain.model.DashboardData
import com.presencial.app.ui.components.CircularProgressCard
import com.presencial.app.ui.components.DashboardProgressBar
import com.presencial.app.ui.components.SmartMessageCard
import com.presencial.app.ui.components.StatCard
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DashboardSmartMessageSection(dashboard: DashboardData) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { ANIM_OFFSET_SMART_MESSAGE })
    ) {
        SmartMessageCard(
            message = dashboard.smartMessage,
            isLoading = dashboard.isLoadingAi
        )
    }
}

@Composable
fun DashboardProgressSection(dashboard: DashboardData) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { ANIM_OFFSET_PROGRESS })
    ) {
        CircularProgressCard(
            progress = dashboard.progressFraction,
            label = "Meta: ${dashboard.requiredPercentage}%",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DashboardProgressBarSection(dashboard: DashboardData) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { ANIM_OFFSET_BAR })
    ) {
        DashboardProgressBar(data = dashboard)
    }
}

@Composable
fun DashboardHeader(dashboard: DashboardData) {
    val monthName = dashboard.yearMonth.month.getDisplayName(
        TextStyle.FULL, 
        Locale.forLanguageTag("pt-BR")
    )
    
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { ANIM_OFFSET_HEADER })
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HEADER_SPACING)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_splash),
                contentDescription = null,
                modifier = Modifier.size(LOGO_SIZE)
            )
            Text(
                text = "${monthName.replaceFirstChar { it.uppercase() }} ${dashboard.yearMonth.year}",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
fun DashboardStats(dashboard: DashboardData) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { ANIM_OFFSET_STATS })
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(STATS_SPACING)
        ) {
            StatCard(
                title = "Úteis",
                value = "${dashboard.workdays}",
                modifier = Modifier.weight(WEIGHT_EQUAL)
            )
            StatCard(
                title = "Meta",
                value = "${dashboard.requiredDays}",
                modifier = Modifier.weight(WEIGHT_EQUAL)
            )
            StatCard(
                title = "Feito",
                value = "${dashboard.completedDays}",
                modifier = Modifier.weight(WEIGHT_EQUAL)
            )
        }
    }
}

@Composable
fun DashboardActionSection(
    dashboard: DashboardData,
    onToggleTodayCheckIn: () -> Unit,
    onMarkYesterdayPresencial: () -> Unit,
    haptic: HapticFeedback
) {
    if (dashboard.todayIsWorkday) {
        CheckInButton(
            isPresencial = dashboard.todayIsPresencial,
            onConfirm = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleTodayCheckIn()
            }
        )
    }

    if (dashboard.yesterdayIsPending) {
        YesterdayCheckInCard(
            onConfirm = onMarkYesterdayPresencial
        )
    }

    if (dashboard.completedDays >= dashboard.requiredDays && dashboard.requiredDays > 0) {
        DashboardSuccessAnimation()
    }
}

@Composable
private fun DashboardSuccessAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.success))
    LottieAnimation(
        composition = composition,
        modifier = Modifier.fillMaxWidth().height(SUCCESS_ANIM_HEIGHT)
    )
}

@Composable
private fun YesterdayCheckInCard(onConfirm: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CORNER_RADIUS_LARGE),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = YESTERDAY_CARD_ALPHA)
        )
    ) {
        Row(
            modifier = Modifier.padding(YESTERDAY_CARD_PADDING),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(WEIGHT_EQUAL)) {
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
        modifier = Modifier.fillMaxWidth().padding(vertical = BUTTON_VERTICAL_PADDING),
        verticalArrangement = Arrangement.spacedBy(CONTENT_SPACING)
    ) {
        if (!isPresencial) {
            ElevatedButton(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(BUTTON_HEIGHT),
                shape = RoundedCornerShape(CORNER_RADIUS_LARGE),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = BUTTON_ELEVATION)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(ICON_SIZE)
                )
                Spacer(Modifier.width(ICON_SPACING))
                Text(
                    text = "Registrar Presença Hoje",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            Button(
                onClick = { },
                enabled = false,
                modifier = Modifier.fillMaxWidth().height(BUTTON_HEIGHT),
                shape = RoundedCornerShape(CORNER_RADIUS_LARGE),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = DISABLED_BUTTON_ALPHA),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(ICON_SPACING))
                Text(
                    text = "Presença Registrada",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

private val LOGO_SIZE = 32.dp
private val HEADER_SPACING = 8.dp
private val STATS_SPACING = 8.dp
private val CONTENT_SPACING = 12.dp
private val SUCCESS_ANIM_HEIGHT = 120.dp
private val BUTTON_HEIGHT = 60.dp
private val BUTTON_VERTICAL_PADDING = 8.dp
private val BUTTON_ELEVATION = 4.dp
private val ICON_SIZE = 24.dp
private val ICON_SPACING = 12.dp
private val YESTERDAY_CARD_PADDING = 12.dp
private val CORNER_RADIUS_LARGE = 16.dp

private const val WEIGHT_EQUAL = 1f
private const val ANIM_OFFSET_HEADER = 40
private const val ANIM_OFFSET_SMART_MESSAGE = 60
private const val ANIM_OFFSET_PROGRESS = 80
private const val ANIM_OFFSET_STATS = 100
private const val ANIM_OFFSET_BAR = 120
private const val YESTERDAY_CARD_ALPHA = 0.5f
private const val DISABLED_BUTTON_ALPHA = 0.5f
