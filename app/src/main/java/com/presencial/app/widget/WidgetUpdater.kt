package com.presencial.app.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager

object WidgetUpdater {

    suspend fun updateAll(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        val widget = PresencialWidget()
        manager.getGlanceIds(widget.javaClass).forEach { glanceId ->
            widget.update(context, glanceId)
        }
    }
}
