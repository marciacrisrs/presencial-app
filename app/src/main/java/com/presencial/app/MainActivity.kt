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
import androidx.lifecycle.lifecycleScope
import com.presencial.app.domain.widget.WidgetRefresher
import com.presencial.app.presentation.navigation.PresencialNavHost
import com.presencial.app.ui.theme.PresencialTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var widgetRefresher: WidgetRefresher

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
        setIntent(intent)
        openCheckIn = intent.getBooleanExtra(EXTRA_OPEN_CHECKIN, false)
    }

    override fun onResume() {
        super.onResume()
        refreshWidget()
    }

    override fun onStop() {
        super.onStop()
        refreshWidget()
    }

    private fun refreshWidget() {
        lifecycleScope.launch {
            withContext(NonCancellable + Dispatchers.IO) {
                widgetRefresher.refresh()
            }
        }
    }

    companion object {
        const val EXTRA_OPEN_CHECKIN = "extra_open_checkin"
    }
}
