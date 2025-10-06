package org.librexray.vpn.presentation.state

import org.librexray.vpn.domain.models.ConnectionSpeed
import org.librexray.vpn.presentation.model.ServerItemModel

data class VpnScreenState(
    val isLaunchLoading: Boolean = true,
    val isRunning: Boolean = false,
    val serverItemList: List<ServerItemModel> = listOf(),
    val selectedServerId: String? = null,
    val delay: Long? = null,
    val connectionSpeed: ConnectionSpeed? = null,
    val wasNotificationPermissionAsked: Boolean = false,
    val error: VpnScreenError? = null,
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