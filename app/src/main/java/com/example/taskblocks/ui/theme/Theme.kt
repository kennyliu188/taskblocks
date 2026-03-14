package com.example.taskblocks.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = NavBrown,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = NavBrownLight,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondary = WarmTextSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    background = WarmBackground,
    onBackground = WarmTextPrimary,
    surface = WarmSurface,
    onSurface = WarmTextPrimary,
    surfaceVariant = WarmSurface,
    onSurfaceVariant = WarmTextSecondary,
    outline = WarmOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = NavBrownLight,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    background = WarmTextPrimary,
    onBackground = WarmSurface,
    surface = NavBrown,
    onSurface = WarmSurface
)

@Composable
fun TaskblocksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).setAppearanceLightStatusBars(!darkTheme)
            }
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
