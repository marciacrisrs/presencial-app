package com.presencial.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.presencial.app.presentation.navigation.PresencialNavHost
import com.presencial.app.ui.theme.PresencialTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var openCheckIn by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        openCheckIn = intent.getBooleanExtra(EXTRA_OPEN_CHECKIN, false)

        setContent {
            PresencialTheme {
                PresencialNavHost(
                    openCheckIn = openCheckIn,
                    onCheckInHandled = { openCheckIn = false }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openCheckIn = intent.getBooleanExtra(EXTRA_OPEN_CHECKIN, false)
    }

    companion object {
        const val EXTRA_OPEN_CHECKIN = "extra_open_checkin"
    }
}
