package com.presencial.app.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition

object WidgetUpdater {

    suspend fun updateAll(context: Context) {
        val appContext = context.applicationContext
        val info = WidgetInfoLoader.load(appContext)
        val manager = GlanceAppWidgetManager(appContext)
        val widget = PresencialWidget()
        glanceIds(manager).forEach { glanceId ->
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
