package org.librexray.vpn.presentation.state

import org.librexray.vpn.domain.models.ThemeMode
import java.util.Locale

data class SettingsScreenState(
    val locale: Locale = Locale.getDefault(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
