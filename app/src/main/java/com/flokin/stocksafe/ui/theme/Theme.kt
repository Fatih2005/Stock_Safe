package com.flokin.stocksafe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary   = Teal80,
    secondary = TealGrey80,
    tertiary  = Cyan80,
    error     = ErrorDark,
    background = SurfaceDark,
    surface    = SurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary   = Teal40,
    secondary = TealGrey40,
    tertiary  = Cyan40,
    error     = ErrorLight,
    background = SurfaceLight,
    surface    = SurfaceLight
)

/**
 * Theme utama aplikasi StockSafe.
 * Mendukung light mode dan dark mode.
 */
@Composable
fun StockSafeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}