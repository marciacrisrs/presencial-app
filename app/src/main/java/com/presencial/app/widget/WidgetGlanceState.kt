package com.presencial.app.widget

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

internal object WidgetGlanceState {
    private val completed = intPreferencesKey("completed")
    private val required = intPreferencesKey("required")
    private val remaining = intPreferencesKey("remaining")
    private val achievedPercentage = intPreferencesKey("achieved_percentage")
    private val monthName = stringPreferencesKey("month_name")
    private val status = stringPreferencesKey("status")
    private val todayStatus = stringPreferencesKey("today_status")
    private val todayIsWorkday = booleanPreferencesKey("today_is_workday")
    private val revision = longPreferencesKey("revision")

    fun write(prefs: MutablePreferences, info: WidgetInfo) {
        prefs[completed] = info.completed
        prefs[required] = info.required
        prefs[remaining] = info.remaining
        prefs[achievedPercentage] = info.achievedPercentage
        prefs[monthName] = info.monthName
        prefs[status] = info.status.name
        prefs[todayStatus] = info.todayStatus.name
        prefs[todayIsWorkday] = info.todayIsWorkday
        prefs[revision] = System.currentTimeMillis()
    }

    fun read(prefs: Preferences): WidgetInfo? {
        return WidgetInfo(
            completed = prefs[completed] ?: return null,
            required = prefs[required] ?: return null,
            remaining = prefs[remaining] ?: return null,
            progressFraction = 0f,
            monthName = prefs[monthName] ?: return null,
            achievedPercentage = prefs[achievedPercentage] ?: 0,
            status = prefs[status]?.let { runCatching { WidgetStatus.valueOf(it) }.getOrNull() }
                ?: return null,
            todayStatus = prefs[todayStatus]?.let {
                runCatching { WidgetTodayStatus.valueOf(it) }.getOrNull()
            } ?: WidgetTodayStatus.PENDING,
            todayIsWorkday = prefs[todayIsWorkday] ?: true
        )
    }
}
