package org.librexray.vpn.presentation.intent

sealed interface VpnScreenIntent {
    object ToggleConnection : VpnScreenIntent
    object TestConnection : VpnScreenIntent
    object ImportConfigFromClipboard : VpnScreenIntent
    data class DeleteItem(val id: String) : VpnScreenIntent
    object RefreshItemList : VpnScreenIntent
    data class SetSelectedServer(val id: String) : VpnScreenIntent
    object ConsumeError: VpnScreenIntent
}