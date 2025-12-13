package com.example.e_faktura.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = LimeAccent,
    onPrimary = NavyDark,
    primaryContainer = NavyLight,
    onPrimaryContainer = LimeLight,
    secondary = NavyLight,
    onSecondary = GreyLight,
    background = NavyPrimary,
    onBackground = GreyLight,
    surface = NavyPrimary,
    onSurface = GreyLight,
    surfaceVariant = NavyLight,
    onSurfaceVariant = GreyMedium,
    error = ErrorRed,
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = White,
    primaryContainer = NavyLight,
    onPrimaryContainer = White,
    secondary = NavyLight,
    onSecondary = White,
    background = GreyLight,
    onBackground = NavyDark,
    surface = White,
    onSurface = NavyDark,
    surfaceVariant = GreyLight,
    onSurfaceVariant = NavyPrimary,
    error = ErrorRed,
    onError = White
)

@Composable
fun EfakturaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    // Usunąłem CompositionLocalProvider z LocalRippleTheme, który powodował błąd.
    // Teraz używamy czystego MaterialTheme 3.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}