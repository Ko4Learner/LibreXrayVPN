package com.pet.vpn_client.presentation.intent

import com.pet.vpn_client.domain.models.AppLocale
import com.pet.vpn_client.domain.models.ThemeMode

interface SettingsScreenIntent {
    data class SetLocale(val locale: AppLocale) : SettingsScreenIntent
    data class SetTheme(val themeMode: ThemeMode) : SettingsScreenIntent
}