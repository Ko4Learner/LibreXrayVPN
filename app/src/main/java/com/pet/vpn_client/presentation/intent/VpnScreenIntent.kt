package com.pet.vpn_client.presentation.intent

sealed interface VpnScreenIntent {
    object ToggleVpnProxy : VpnScreenIntent
    object SwitchVpnProxy : VpnScreenIntent
    object TestConnection : VpnScreenIntent
    object RestartConnection : VpnScreenIntent
    object ImportConfigFromClipboard : VpnScreenIntent
}