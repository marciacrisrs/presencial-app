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
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
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

    @Suppress("RestrictedApi")
    val backgroundProvider = ColorProvider(R.color.widget_background)
    @Suppress("RestrictedApi")
    val successProvider = ColorProvider(R.color.widget_success)
    @Suppress("RestrictedApi")
    val secondaryTextProvider = ColorProvider(R.color.widget_text_secondary)

    val modifier = GlanceModifier
        .fillMaxSize()
        .background(backgroundProvider)
        .cornerRadius(WIDGET_CORNER_RADIUS.dp)
        .padding(WIDGET_PADDING.dp)

    when (widgetSize) {
        WidgetSize.SMALL -> SmallLayout(completed, required, successProvider, modifier)
        WidgetSize.MEDIUM -> MediumLayout(completed, required, remaining, successProvider, secondaryTextProvider, modifier)
        WidgetSize.LARGE -> LargeLayout(monthName, completed, required, remaining, successProvider, secondaryTextProvider, modifier)
    }
}

@Composable
private fun SmallLayout(completed: Int, required: Int, successProvider: ColorProvider, modifier: GlanceModifier) {
    Column(
        modifier = modifier,
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = "$completed / $required",
            style = TextStyle(color = successProvider, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        )
    }
}

@Composable
private fun MediumLayout(completed: Int, required: Int, remaining: Int, successProvider: ColorProvider, secondaryTextProvider: ColorProvider, modifier: GlanceModifier) {
    Column(
        modifier = modifier,
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = "$completed / $required",
            style = TextStyle(color = successProvider, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = if (remaining == 1) "Falta $remaining dia" else "Faltam $remaining dias",
            style = TextStyle(color = secondaryTextProvider, fontSize = 13.sp, textAlign = TextAlign.Center)
        )
    }
}

@Composable
private fun LargeLayout(monthName: String, completed: Int, required: Int, remaining: Int, successProvider: ColorProvider, secondaryTextProvider: ColorProvider, modifier: GlanceModifier) {
    val progress = if (required > 0) (completed.toFloat() / required).coerceIn(0f, 1f) else 0f

    Column(
        modifier = modifier,
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = "PRESENÇA EM $monthName",
            style = TextStyle(color = secondaryTextProvider, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        )
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        Text(
            text = "$completed / $required",
            style = TextStyle(color = successProvider, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = GlanceModifier.height(10.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = GlanceModifier.fillMaxWidth().height(6.dp),
            color = successProvider,
            backgroundColor = secondaryTextProvider
        )

        Spacer(modifier = GlanceModifier.height(10.dp))

        Text(
            text = if (remaining == 1) "Falta $remaining dia" else "Faltam $remaining dias",
            style = TextStyle(color = secondaryTextProvider, fontSize = 13.sp)
        )
    }
}

private const val WIDGET_CORNER_RADIUS = 24
private const val WIDGET_PADDING = 12
private const val WIDGET_FONT_SIZE_LABEL = 14
private const val WIDGET_FONT_SIZE_MAIN = 42
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
