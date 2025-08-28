package org.librexray.vpn.presentation.intent

import org.librexray.vpn.domain.models.AppLocale
import org.librexray.vpn.domain.models.ThemeMode

interface SettingsScreenIntent {
    data class SetLocale(val locale: AppLocale) : SettingsScreenIntent
    data class SetTheme(val themeMode: ThemeMode) : SettingsScreenIntent
}