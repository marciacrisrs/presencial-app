package com.presencial.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.annotation.SuppressLint
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
            PresencialWidgetContent()
        }
    }

    @Composable
    private fun PresencialWidgetContent() {
        val prefs = currentState<Preferences>()
        val info = WidgetGlanceState.read(prefs)
        val colors = WidgetColors.from()
        val context = androidx.glance.LocalContext.current
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(colors.background)
                .cornerRadius(WIDGET_CORNER_RADIUS.dp)
                .padding(WIDGET_PADDING.dp)
                .clickable(actionStartActivity(intent)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = info.monthName,
                style = TextStyle(
                    color = colors.secondaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "${info.completed}/${info.required}",
                style = TextStyle(
                    color = colors.headline(info.status),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = "${info.achievedPercentage}%",
                style = TextStyle(
                    color = colors.primaryText,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

private data class WidgetColors(
    val background: ColorProvider,
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
        @SuppressLint("RestrictedApi")
        fun from(): WidgetColors = WidgetColors(
            background = ColorProvider(R.color.widget_background),
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
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                appWidgetIds.forEach { appWidgetId ->
                    glanceAppWidget.update(context, androidx.glance.appwidget.AppWidgetId(appWidgetId))
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
