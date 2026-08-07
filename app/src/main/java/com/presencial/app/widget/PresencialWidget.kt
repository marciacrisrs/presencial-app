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
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
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

class PresencialWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent(context)
            }
        }
    }
}

@Composable
private fun WidgetContent(context: Context) {
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

    @Suppress("RestrictedApi")
    val backgroundProvider = ColorProvider(R.color.widget_background)
    @Suppress("RestrictedApi")
    val successProvider = ColorProvider(R.color.widget_success)
    @Suppress("RestrictedApi")
    val secondaryTextProvider = ColorProvider(R.color.widget_text_secondary)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundProvider)
            .cornerRadius(WIDGET_CORNER_RADIUS.dp)
            .padding(WIDGET_PADDING.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = "Faltam",
            style = TextStyle(
                color = secondaryTextProvider,
                fontSize = WIDGET_FONT_SIZE_LABEL.sp,
                textAlign = TextAlign.Center
            )
        )
        Text(
            text = "$remaining",
            style = TextStyle(
                color = successProvider,
                fontSize = WIDGET_FONT_SIZE_MAIN.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        Text(
            text = if (remaining == ONE_DAY) "dia presencial" else "dias presenciais",
            style = TextStyle(
                color = secondaryTextProvider,
                fontSize = WIDGET_FONT_SIZE_LABEL.sp,
                textAlign = TextAlign.Center
            )
        )
    }
}

private const val WIDGET_CORNER_RADIUS = 24
private const val WIDGET_PADDING = 16
private const val WIDGET_FONT_SIZE_LABEL = 14
private const val WIDGET_FONT_SIZE_MAIN = 42
private const val ONE_DAY = 1
private const val WIDGET_PREFS = "presencial_settings"
private const val PREF_REQUIRED_PERCENTAGE = "required_percentage"
private const val PREF_COUNT_SATURDAYS = "count_saturdays_as_workdays"
private const val DEFAULT_REQUIRED_PERCENTAGE = 40


class PresencialWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PresencialWidget()
}
