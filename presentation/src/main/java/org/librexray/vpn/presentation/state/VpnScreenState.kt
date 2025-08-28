package org.librexray.vpn.presentation.state

import org.librexray.vpn.domain.models.ConnectionSpeed
import org.librexray.vpn.presentation.models.ServerItemModel

data class VpnScreenState(
    val isLoading: Boolean = false,
    val isRunning: Boolean = false,
    val serverItemList: List<ServerItemModel> = listOf(),
    val delay: Long? = null,
    val error: String? = null,
    val selectedServerId: String? = null,
    val connectionSpeed: ConnectionSpeed? = null
)
