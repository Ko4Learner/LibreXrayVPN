package com.pet.vpn_client.presentation.state

import com.pet.vpn_client.domain.models.ThemeMode
import java.util.Locale

data class SettingsScreenState(
    val locale: Locale = Locale.getDefault(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
