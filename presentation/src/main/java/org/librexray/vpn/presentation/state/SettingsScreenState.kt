package org.librexray.vpn.presentation.state

import org.librexray.vpn.domain.models.AppLocale
import org.librexray.vpn.domain.models.ThemeMode

data class SettingsScreenState(
    val localeMode: AppLocale = AppLocale.SYSTEM,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
