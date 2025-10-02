package org.librexray.vpn.presentation.design_system.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import org.librexray.vpn.domain.models.ThemeMode

private val DarkColorPalette = darkColors(
    primary = Blue,
    secondary = Grey,
    background = Black,
    surface = Grey,
    onPrimary = White,
    onSecondary = White,
    onBackground = White,
    onSurface = White
)

private val LightColorPalette = lightColors(
    primary = Blue,
    secondary = MistBlue,
    background = Ice,
    surface = MistBlue,
    onPrimary = Ink,
    onSecondary = Ink,
    onBackground = Ink,
    onSurface = Ink
)

@Composable
fun LibreXrayVPNTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colors = if (dark) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors = colors,
        typography = AppTypography,
        content = content
    )
}