package com.presencial.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.datastore.preferences.core.Preferences
import com.presencial.app.MainActivity
import com.presencial.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PresencialWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val loaded = WidgetInfoLoader.load(context)
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
            prefs.toMutablePreferences().apply {
                WidgetGlanceState.write(this, loaded)
            }
        }
        provideContent {
            val info = WidgetGlanceState.read(currentState<Preferences>()) ?: loaded
            GlanceTheme {
                WidgetContent(info)
            }
        }
    }
}

@Composable
private fun WidgetContent(info: WidgetInfo) {
    val context = androidx.glance.LocalContext.current
    val colors = WidgetColors.from()

    val modifier = GlanceModifier
        .fillMaxSize()
        .background(R.color.widget_background)
        .cornerRadius(WIDGET_CORNER_RADIUS.dp)
        .clickable(
            actionStartActivity(
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            )
        )
        .padding(WIDGET_PADDING.dp)

    Column(
        modifier = modifier,
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = context.getString(R.string.widget_title_month, info.monthName),
            style = TextStyle(
                color = colors.secondaryText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        Text(
            text = headlineFor(info, context),
            style = TextStyle(
                color = colors.headline(info.status),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )

        val remaining = remainingLine(info, context)
        if (remaining.isNotEmpty()) {
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = remaining,
                style = TextStyle(
                    color = colors.secondaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )
        }

        if (info.todayIsWorkday) {
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = todayLabel(info.todayStatus, context),
                style = TextStyle(
                    color = when (info.todayStatus) {
                        WidgetTodayStatus.PRESENCIAL -> colors.success
                        WidgetTodayStatus.HOME_OFFICE -> colors.primaryText
                        WidgetTodayStatus.PENDING -> colors.secondaryText
                    },
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

private fun todayLabel(status: WidgetTodayStatus, context: Context): String = when (status) {
    WidgetTodayStatus.PRESENCIAL -> context.getString(R.string.widget_today_presencial)
    WidgetTodayStatus.HOME_OFFICE -> context.getString(R.string.widget_today_home_office)
    WidgetTodayStatus.PENDING -> context.getString(R.string.widget_today_pending)
}

private fun headlineFor(info: WidgetInfo, context: Context): String =
    if (info.required <= 0) {
        context.getString(R.string.widget_configure_goal)
    } else {
        context.getString(R.string.widget_compact_progress, info.completed, info.required)
    }

private fun remainingLine(info: WidgetInfo, context: Context): String = when {
    info.required <= 0 -> ""
    info.status == WidgetStatus.GOAL_MET -> context.getString(R.string.widget_goal_met)
    else -> context.resources.getQuantityString(
        R.plurals.widget_remaining_days,
        info.remaining,
        info.remaining
    )
}

private data class WidgetColors(
    val success: ColorProvider,
    val warning: ColorProvider,
    val primaryText: ColorProvider,
    val secondaryText: ColorProvider
) {
    fun headline(status: WidgetStatus): ColorProvider = when (status) {
        WidgetStatus.BEHIND -> warning
        WidgetStatus.GOAL_MET -> success
        else -> primaryText
    }

    companion object {
        fun from(): WidgetColors = WidgetColors(
            success = ColorProvider(R.color.widget_success),
            warning = ColorProvider(R.color.widget_warning),
            primaryText = ColorProvider(R.color.widget_primary_text),
            secondaryText = ColorProvider(R.color.widget_text_secondary)
        )
    }
}

private const val WIDGET_CORNER_RADIUS = 16
private const val WIDGET_PADDING = 8

class PresencialWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PresencialWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // GlanceAppWidgetReceiver already calls goAsync(); a second call returns null
        // and pendingResult.finish() crashes the process on Samsung.
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            WidgetUpdater.updateAll(context)
        }
    }
}
