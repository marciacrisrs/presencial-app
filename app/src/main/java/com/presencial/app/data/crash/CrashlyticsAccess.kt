package com.presencial.app.data.crash

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

internal object CrashlyticsAccess {
    fun ensureInitialized(context: Context) {
        runCatching {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
        }
    }

    fun getOrNull(): FirebaseCrashlytics? =
        runCatching { FirebaseCrashlytics.getInstance() }.getOrNull()
}
