package org.librexray.vpn.presentation.state

import org.librexray.vpn.domain.models.ConnectionSpeed
import org.librexray.vpn.presentation.models.ServerItemModel

data class VpnScreenState(
    val isLoading: Boolean = true,
    val isRunning: Boolean = false,
    val serverItemList: List<ServerItemModel> = listOf(),
    val delay: Long? = null,
    val error: VpnScreenError? = null,
    val selectedServerId: String? = null,
    val connectionSpeed: ConnectionSpeed? = null,
    val wasNotificationPermissionAsked: Boolean = false
)

sealed interface VpnScreenError {
    object StartError : VpnScreenError
    object StopError : VpnScreenError
    object ImportConfigError : VpnScreenError
    object EmptyConfigError : VpnScreenError
    object DeleteConfigError : VpnScreenError
    object TestConnectionError : VpnScreenError
    object UpdateServerListError : VpnScreenError
    object SelectServerError : VpnScreenError
}