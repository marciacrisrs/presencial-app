package com.presencial.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
    val prefs = context.getSharedPreferences("presencial_settings", Context.MODE_PRIVATE)
    val percentage = prefs.getInt("required_percentage", 40)
    val countSaturdays = prefs.getBoolean("count_saturdays_as_workdays", false)

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

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF1E1E1E)))
            .cornerRadius(24.dp)
            .padding(16.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = "Faltam",
            style = TextStyle(
                color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        )
        Text(
            text = "$remaining",
            style = TextStyle(
                color = ColorProvider(Color(0xFF81C995)),
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        Text(
            text = if (remaining == 1) "dia presencial" else "dias presenciais",
            style = TextStyle(
                color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        )
    }
}

class PresencialWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PresencialWidget()
}
