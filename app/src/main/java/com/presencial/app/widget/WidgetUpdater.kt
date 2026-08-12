package com.presencial.app.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager

object WidgetUpdater {

    suspend fun updateAll(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        listOf(
            PresencialWidgetSmall(),
            PresencialWidgetMedium(),
            PresencialWidgetLarge()
        ).forEach { widget ->
            manager.getGlanceIds(widget.javaClass).forEach { glanceId ->
                widget.update(context, glanceId)
            }
        }
    }
}
