package com.presencial.app.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition

object WidgetUpdater {

    suspend fun updateAll(context: Context) {
        val appContext = context.applicationContext
        val manager = GlanceAppWidgetManager(appContext)
        val ids = runCatching { glanceIds(manager) }.getOrDefault(emptyList())
        if (ids.isEmpty()) return
        val info = WidgetInfoLoader.load(appContext)
        val widget = PresencialWidget()
        ids.forEach { glanceId ->
            updateAppWidgetState(appContext, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    WidgetGlanceState.write(this, info)
                }
            }
            widget.update(appContext, glanceId)
        }
    }

    private suspend fun glanceIds(manager: GlanceAppWidgetManager): List<GlanceId> =
        manager.getGlanceIds(PresencialWidget::class.java)
}
