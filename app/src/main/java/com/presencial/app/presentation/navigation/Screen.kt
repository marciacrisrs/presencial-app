package com.presencial.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Dashboard : Screen("dashboard", "Início", Icons.Default.Home)
    data object Calendar : Screen("calendar", "Calendário", Icons.Default.CalendarMonth)
    data object History : Screen("history", "Histórico", Icons.Default.History)
    data object Statistics : Screen("statistics", "Estatísticas", Icons.Default.BarChart)
    data object Settings : Screen("settings", "Configurações", Icons.Default.Settings)
    data object About : Screen("about", "Sobre")
    data object Absences : Screen("absences", "Ausências")
    data object WorkAddresses : Screen("work_addresses", "Locais de Trabalho")

    companion object {
        const val MAIN_ROUTE = "main"
        const val TAB_ARG = "tab"

        val bottomNavItems = listOf(Dashboard, Calendar, History, Settings)

        fun mainRoute(tab: Int = 0): String = "$MAIN_ROUTE?$TAB_ARG=$tab"

        fun isMainDestination(route: String?): Boolean =
            route?.startsWith("$MAIN_ROUTE?") == true

        fun tabFromRoute(route: String?): Int? {
            if (!isMainDestination(route)) return null
            return route
                ?.substringAfter("$TAB_ARG=")
                ?.substringBefore("&")
                ?.toIntOrNull()
        }
    }
}
