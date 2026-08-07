package com.presencial.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private const val COLOR_PRIMARY = 0xFF1B873B
private const val COLOR_PRIMARY_DARK = 0xFF81C995
private const val COLOR_ON_PRIMARY_DARK = 0xFF003910
private const val COLOR_PRIMARY_CONTAINER = 0xFFC8E6C9
private const val COLOR_PRIMARY_CONTAINER_DARK = 0xFF0D652D
private const val COLOR_ACCENT = 0xFF1A73E8
private const val COLOR_ACCENT_DARK = 0xFF8AB4F8
private const val COLOR_SURFACE_LIGHT = 0xFFF8F9FA
private const val COLOR_ON_SURFACE_LIGHT = 0xFF202124
private const val COLOR_SURFACE_VARIANT_LIGHT = 0xFFE8EAED
private const val COLOR_ERROR_LIGHT = 0xFFD93025
private const val COLOR_SURFACE_DARK = 0xFF1E1E1E
private const val COLOR_ON_SURFACE_DARK = 0xFFE8EAED
private const val COLOR_SURFACE_VARIANT_DARK = 0xFF3C4043
private const val COLOR_ERROR_DARK = 0xFFF28B82
private const val COLOR_BACKGROUND_DARK = 0xFF121212

private val LightColors = lightColorScheme(
    primary = Color(COLOR_PRIMARY),
    onPrimary = Color.White,
    primaryContainer = Color(COLOR_PRIMARY_CONTAINER),
    secondary = Color(COLOR_ACCENT),
    background = Color.White,
    surface = Color(COLOR_SURFACE_LIGHT),
    onSurface = Color(COLOR_ON_SURFACE_LIGHT),
    surfaceVariant = Color(COLOR_SURFACE_VARIANT_LIGHT),
    error = Color(COLOR_ERROR_LIGHT)
)

private val DarkColors = darkColorScheme(
    primary = Color(COLOR_PRIMARY_DARK),
    onPrimary = Color(COLOR_ON_PRIMARY_DARK),
    primaryContainer = Color(COLOR_PRIMARY_CONTAINER_DARK),
    secondary = Color(COLOR_ACCENT_DARK),
    background = Color(COLOR_BACKGROUND_DARK),
    surface = Color(COLOR_SURFACE_DARK),
    onSurface = Color(COLOR_ON_SURFACE_DARK),
    surfaceVariant = Color(COLOR_SURFACE_VARIANT_DARK),
    error = Color(COLOR_ERROR_DARK)
)

@Composable
fun PresencialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PresencialTypography,
        content = content
    )
}
