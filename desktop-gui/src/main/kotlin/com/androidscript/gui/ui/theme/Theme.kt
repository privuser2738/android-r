package com.androidscript.gui.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Material Design 3 Colors
val md_theme_light_primary = Color(0xFF2196F3)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_secondary = Color(0xFF03DAC6)
val md_theme_light_error = Color(0xFFF44336)
val md_theme_light_background = Color(0xFFFAFAFA)
val md_theme_light_surface = Color(0xFFFFFFFF)

val md_theme_dark_primary = Color(0xFF64B5F6)
val md_theme_dark_onPrimary = Color(0xFF000000)
val md_theme_dark_secondary = Color(0xFF03DAC6)
val md_theme_dark_error = Color(0xFFEF5350)
val md_theme_dark_background = Color(0xFF212121)
val md_theme_dark_surface = Color(0xFF424242)

val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    secondary = md_theme_light_secondary,
    error = md_theme_light_error,
    background = md_theme_light_background,
    surface = md_theme_light_surface
)

val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    secondary = md_theme_dark_secondary,
    error = md_theme_dark_error,
    background = md_theme_dark_background,
    surface = md_theme_dark_surface
)

// Custom colors
val AndroidColor = Color(0xFF4CAF50)
val IOSColor = Color(0xFF000000)
val SuccessColor = Color(0xFF4CAF50)
val WarningColor = Color(0xFFFF9800)
val InfoColor = Color(0xFF2196F3)
