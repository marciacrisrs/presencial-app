package com.presencial.app.domain.widget

fun interface WidgetRefresher {
    suspend fun refresh()
}
