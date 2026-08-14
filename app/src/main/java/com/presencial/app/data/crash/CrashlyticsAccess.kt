package com.presencial.app.data.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics

internal object CrashlyticsAccess {
    fun getOrNull(): FirebaseCrashlytics? =
        runCatching { FirebaseCrashlytics.getInstance() }.getOrNull()
}
