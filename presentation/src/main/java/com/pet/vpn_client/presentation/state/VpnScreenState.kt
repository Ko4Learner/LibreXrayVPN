package com.pet.vpn_client.presentation.state

import com.pet.vpn_client.domain.models.ConnectionSpeed
import com.pet.vpn_client.presentation.models.ServerItemModel

data class VpnScreenState(
    val isLoading: Boolean = false,
    val isRunning: Boolean = false,
    val serverItemList: List<ServerItemModel> = listOf(),
    val delay: Long? = null,
    val error: String? = null,
    val selectedServerId: String? = null,
    val connectionSpeed: ConnectionSpeed? = null
)
