package com.pet.vpn_client.presentation.state

import com.pet.vpn_client.presentation.models.ServerItemModel

data class VpnScreenState(
    val isLoading: Boolean = false,
    val isRunning: Boolean = false,
    val isVpnMode: Boolean = true,
    val serverItemList: List<ServerItemModel> = listOf(),
    val error: String? = null,
)
