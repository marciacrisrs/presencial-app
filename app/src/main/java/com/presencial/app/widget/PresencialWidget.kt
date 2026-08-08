package com.presencial.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.presencial.app.R
import com.presencial.app.data.local.PresencialDatabase
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.util.GoalCalculator
import com.presencial.app.domain.util.WorkdayCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.YearMonth
import java.util.Locale

abstract class BasePresencialWidget(private val widgetSize: WidgetSize) : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent(context, widgetSize)
            }
        }
    }
}

class PresencialWidgetSmall : BasePresencialWidget(WidgetSize.SMALL)
class PresencialWidgetMedium : BasePresencialWidget(WidgetSize.MEDIUM)
class PresencialWidgetLarge : BasePresencialWidget(WidgetSize.LARGE)

@Composable
private fun WidgetContent(context: Context, widgetSize: WidgetSize) {
    val db = PresencialDatabase.getInstance(context)
    val prefs = context.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE)
    val percentage = prefs.getInt(PREF_REQUIRED_PERCENTAGE, DEFAULT_REQUIRED_PERCENTAGE)
    val countSaturdays = prefs.getBoolean(PREF_COUNT_SATURDAYS, false)

    val yearMonth = YearMonth.now()
    val monthName = yearMonth.month.getDisplayName(
        java.time.format.TextStyle.FULL, 
        Locale.forLanguageTag("pt-BR")
    ).uppercase()
    
    val workdays = WorkdayCalculator.countWorkdaysInMonth(yearMonth, countSaturdays)
    val required = GoalCalculator.calculateRequiredDays(workdays, percentage)

    val start = yearMonth.atDay(1).toEpochDay()
    val end = yearMonth.atEndOfMonth().toEpochDay()
    val checkIns = runBlocking {
        db.checkInDao().observeBetween(start, end).first()
    }
    val completed = checkIns.count { it.status == DayStatus.PRESENCIAL.name }
    val remaining = GoalCalculator.calculateRemainingDays(completed, required)

    val progress = WidgetProgress(completed, required, remaining)

    @Suppress("RestrictedApi")
    val backgroundProvider = ColorProvider(R.color.widget_background)
    @Suppress("RestrictedApi")
    val successProvider = ColorProvider(R.color.widget_success)
    @Suppress("RestrictedApi")
    val secondaryTextProvider = ColorProvider(R.color.widget_text_secondary)

    val colors = WidgetColors(successProvider, secondaryTextProvider)

    val modifier = GlanceModifier
        .fillMaxSize()
        .background(backgroundProvider)
        .cornerRadius(WIDGET_CORNER_RADIUS.dp)
        .padding(WIDGET_PADDING.dp)

    when (widgetSize) {
        WidgetSize.SMALL -> SmallLayout(progress, colors, modifier)
        WidgetSize.MEDIUM -> MediumLayout(progress, colors, modifier)
        WidgetSize.LARGE -> LargeLayout(monthName, progress, colors, modifier)
    }
}

@Composable
private fun SmallLayout(
    progress: WidgetProgress,
    colors: WidgetColors,
    modifier: GlanceModifier
) {
    Column(
        modifier = modifier,
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = "${progress.completed} / ${progress.required}",
            style = TextStyle(
                color = colors.success,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
private fun MediumLayout(
    progress: WidgetProgress,
    colors: WidgetColors,
    modifier: GlanceModifier
) {
    Column(
        modifier = modifier,
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = "${progress.completed} / ${progress.required}",
            style = TextStyle(
                color = colors.success,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = if (progress.remaining == 1) {
                "Falta ${progress.remaining} dia"
            } else {
                "Faltam ${progress.remaining} dias"
            },
            style = TextStyle(
                color = colors.secondaryText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
private fun LargeLayout(
    monthName: String,
    progress: WidgetProgress,
    colors: WidgetColors,
    modifier: GlanceModifier
) {
    val progressFraction = if (progress.required > 0) {
        (progress.completed.toFloat() / progress.required).coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        modifier = modifier,
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = "PRESENÇA EM $monthName",
            style = TextStyle(
                color = colors.secondaryText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        )
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        Text(
            text = "${progress.completed} / ${progress.required}",
            style = TextStyle(
                color = colors.success,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = GlanceModifier.height(10.dp))

        LinearProgressIndicator(
            progress = progressFraction,
            modifier = GlanceModifier.fillMaxWidth().height(6.dp),
            color = colors.success,
            backgroundColor = colors.secondaryText
        )

        Spacer(modifier = GlanceModifier.height(10.dp))

        Text(
            text = if (progress.remaining == 1) {
                "Falta ${progress.remaining} dia"
            } else {
                "Faltam ${progress.remaining} dias"
            },
            style = TextStyle(
                color = colors.secondaryText,
                fontSize = 13.sp
            )
        )
    }
}

private data class WidgetProgress(
    val completed: Int,
    val required: Int,
    val remaining: Int
)

private data class WidgetColors(
    val success: ColorProvider,
    val secondaryText: ColorProvider
)

private const val WIDGET_CORNER_RADIUS = 24
private const val WIDGET_PADDING = 12
private const val WIDGET_PREFS = "presencial_settings"
private const val PREF_REQUIRED_PERCENTAGE = "required_percentage"
private const val PREF_COUNT_SATURDAYS = "count_saturdays_as_workdays"
private const val DEFAULT_REQUIRED_PERCENTAGE = 40

class PresencialWidgetReceiverSmall : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PresencialWidgetSmall()
}
class PresencialWidgetReceiverMedium : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PresencialWidgetMedium()
}
class PresencialWidgetReceiverLarge : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PresencialWidgetLarge()
}
