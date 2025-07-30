package com.pet.vpn_client.presentation.state

import java.util.Locale

data class SettingsScreenState(
    val locale: Locale = Locale.getDefault(),
    val nightMode: Boolean = false
)
