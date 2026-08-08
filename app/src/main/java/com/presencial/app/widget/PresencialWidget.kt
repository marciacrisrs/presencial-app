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
    
    val workdays = WorkdayCalculator.countWorkdaysInMonth(yearMonth, countSaturdays)
    val required = GoalCalculator.calculateRequiredDays(workdays, percentage)

    val start = yearMonth.atDay(1).toEpochDay()
    val end = yearMonth.atEndOfMonth().toEpochDay()
    val checkIns = runBlocking {
        db.checkInDao().observeBetween(start, end).first()
    }
    val completed = checkIns.count { it.status == DayStatus.PRESENCIAL.name }
    val remaining = GoalCalculator.calculateRemainingDays(completed, required)

    val info = WidgetInfo.create(completed, required, remaining, yearMonth)

    val successColor = androidx.compose.ui.graphics.Color(context.getColor(R.color.widget_success))
    val secondaryColor = androidx.compose.ui.graphics.Color(context.getColor(R.color.widget_text_secondary))

    val successProvider = androidx.glance.color.ColorProvider(day = successColor, night = successColor)
    val secondaryTextProvider = androidx.glance.color.ColorProvider(day = secondaryColor, night = secondaryColor)

    val colors = WidgetColors(successProvider, secondaryTextProvider)

    val modifier = GlanceModifier
        .fillMaxSize()
        .background(R.color.widget_background)
        .cornerRadius(WIDGET_CORNER_RADIUS.dp)
        .padding(WIDGET_PADDING.dp)

    when (widgetSize) {
        WidgetSize.SMALL -> SmallLayout(info, colors, modifier)
        WidgetSize.MEDIUM -> MediumLayout(info, colors, modifier)
        WidgetSize.LARGE -> LargeLayout(info, colors, modifier)
    }
}

@Composable
private fun SmallLayout(
    info: WidgetInfo,
    colors: WidgetColors,
    modifier: GlanceModifier
) {
    Column(
        modifier = modifier,
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = androidx.glance.LocalContext.current.getString(
                R.string.widget_progress_format,
                info.completed,
                info.required
            ),
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
    info: WidgetInfo,
    colors: WidgetColors,
    modifier: GlanceModifier
) {
    Column(
        modifier = modifier,
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = androidx.glance.LocalContext.current.getString(
                R.string.widget_progress_format,
                info.completed,
                info.required
            ),
            style = TextStyle(
                color = colors.success,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = androidx.glance.LocalContext.current.resources.getQuantityString(
                R.plurals.widget_remaining_days,
                info.remaining,
                info.remaining
            ),
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
    info: WidgetInfo,
    colors: WidgetColors,
    modifier: GlanceModifier
) {
    Column(
        modifier = modifier,
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = androidx.glance.LocalContext.current.getString(
                R.string.widget_title_month,
                info.monthName
            ),
            style = TextStyle(
                color = colors.secondaryText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        Text(
            text = androidx.glance.LocalContext.current.getString(
                R.string.widget_progress_format,
                info.completed,
                info.required
            ),
            style = TextStyle(
                color = colors.success,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = GlanceModifier.height(10.dp))

        LinearProgressIndicator(
            progress = info.progressFraction,
            modifier = GlanceModifier.fillMaxWidth().height(6.dp),
            color = colors.success,
            backgroundColor = colors.secondaryText
        )

        Spacer(modifier = GlanceModifier.height(10.dp))

        Text(
            text = androidx.glance.LocalContext.current.resources.getQuantityString(
                R.plurals.widget_remaining_days,
                info.remaining,
                info.remaining
            ),
            style = TextStyle(
                color = colors.secondaryText,
                fontSize = 13.sp
            )
        )
    }
}

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
