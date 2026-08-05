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

private val GreenPrimary = Color(0xFF1B873B)
private val GreenDark = Color(0xFF0D652D)
private val BlueAccent = Color(0xFF1A73E8)
private val SurfaceLight = Color(0xFFF8F9FA)
private val SurfaceDark = Color(0xFF1E1E1E)

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    secondary = BlueAccent,
    background = Color.White,
    surface = SurfaceLight,
    onSurface = Color(0xFF202124),
    surfaceVariant = Color(0xFFE8EAED),
    error = Color(0xFFD93025)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF81C995),
    onPrimary = Color(0xFF003910),
    primaryContainer = GreenDark,
    secondary = Color(0xFF8AB4F8),
    background = Color(0xFF121212),
    surface = SurfaceDark,
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF3C4043),
    error = Color(0xFFF28B82)
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
