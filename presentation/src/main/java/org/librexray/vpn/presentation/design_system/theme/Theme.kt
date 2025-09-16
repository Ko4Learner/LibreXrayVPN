package org.librexray.vpn.presentation.design_system.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Blue,           // главный акцент (кнопка "Вкл", ссылки)
    secondary = Grey,         // второй акцент (кнопка "Выкл", иконки, второстепенные действия)
    background = Black20,     // общий фон экрана
    surface = Grey,           // карточки, панели, плашки (чуть светлее фона, у тебя это Grey)
    onPrimary = White,        // контент на primary (иконка выключателя/текст "Вкл")
    onSecondary = White,      // контент на secondary (текст/иконки поверх серого круга/кнопки)
    onBackground = White,     // основной текст на фоне
    onSurface = White         // текст на карточках/плашках
)

private val LightColorPalette = lightColors(
    primary = Blue,           // главный акцент (кнопка "Вкл", ссылки)
    secondary = Grey,         // второй акцент (кнопка "Выкл", иконки, второстепенные действия)
    background = Black20,     // общий фон экрана
    surface = Grey,           // карточки, панели, плашки (чуть светлее фона, у тебя это Grey)
    onPrimary = White,        // контент на primary (иконка выключателя/текст "Вкл")
    onSecondary = White,      // контент на secondary (текст/иконки поверх серого круга/кнопки)
    onBackground = White,     // основной текст на фоне
    onSurface = White
)

@Composable
fun LibreXrayVPNTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors = colors,
        typography = AppTypography,
        content = content
    )
}