package com.pet.vpn_client.presentation.state

data class QrCodeScreenState(
    val configFound: Boolean = false,
    val error: String? = null
)
