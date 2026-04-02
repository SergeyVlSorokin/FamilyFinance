package com.familyfinance.ui.theme

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

// @trace TASK-121
private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = OnGreen80,
    primaryContainer = GreenContainer30,
    onPrimaryContainer = OnGreenContainer90,
    secondary = SecondaryGreen80,
    onSecondary = OnSecondaryGreen80,
    secondaryContainer = SecondaryGreenContainer30,
    onSecondaryContainer = OnSecondaryGreenContainer90,
    tertiary = TertiaryGreen80,
    onTertiary = OnTertiaryGreen80,
    tertiaryContainer = TertiaryGreenContainer30,
    onTertiaryContainer = OnTertiaryGreenContainer90,
    background = GreenBackgroundDark,
    onBackground = OnGreenBackgroundDark,
    surface = GreenSurfaceDark,
    onSurface = OnGreenSurfaceDark,
    surfaceVariant = GreenSurfaceVariantDark,
    onSurfaceVariant = OnGreenSurfaceVariantDark
)

// @trace TASK-121
private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = OnGreen40,
    primaryContainer = GreenContainer90,
    onPrimaryContainer = OnGreenContainer10,
    secondary = SecondaryGreen40,
    onSecondary = OnSecondaryGreen40,
    secondaryContainer = SecondaryGreenContainer90,
    onSecondaryContainer = OnSecondaryGreenContainer10,
    tertiary = TertiaryGreen40,
    onTertiary = OnTertiaryGreen40,
    tertiaryContainer = TertiaryGreenContainer90,
    onTertiaryContainer = OnTertiaryGreenContainer10,
    background = GreenBackgroundLight,
    onBackground = OnGreenBackgroundLight,
    surface = GreenSurfaceLight,
    onSurface = OnGreenSurfaceLight,
    surfaceVariant = GreenSurfaceVariantLight,
    onSurfaceVariant = OnGreenSurfaceVariantLight
)

@Composable
fun FamilyFinanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            // primary in light mode is dark green (needs light icons = false)
            // primary in dark mode is light green (needs dark icons = true)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
