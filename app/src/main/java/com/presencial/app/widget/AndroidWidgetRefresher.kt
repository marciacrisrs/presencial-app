package com.presencial.app.widget

import android.content.Context
import com.presencial.app.domain.widget.WidgetRefresher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidWidgetRefresher @Inject constructor(
    @ApplicationContext private val context: Context
) : WidgetRefresher {
    override suspend fun refresh() {
        runCatching { WidgetUpdater.updateAll(context) }
    }
}
